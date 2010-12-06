//
// $Id$

package memory.server

import scala.collection.mutable.{Seq => MSeq}
import scalaj.collection.Imports._

import java.util.{Map => JMap, List => JList, ArrayList => JArrayList}
import com.google.gwt.user.server.rpc.RemoteServiceServlet

import com.google.appengine.api.users.{User, UserServiceFactory}
import com.google.appengine.api.blobstore.{BlobstoreServiceFactory}

import memory.data.{Access, AccessInfo, Datum, FieldValue, Type}
import memory.persist.DB
import memory.rpc.{DataService, ServiceException}

/**
 * Implements the {@link DataService}.
 */
class DataServlet extends RemoteServiceServlet with DataService
{
  // TODO: configure this based on servlet config?
  val db :DB = memory.persist.objectify.ObjectifyDB

  // from DataService
  def loadAccountInfo = {
    val user = _usvc.getCurrentUser
    if (user == null) {
      throw new ServiceException("e.not_logged_in")
    }

    val result = new DataService.AccountResult
    result.userId = user.getUserId
    result.nickname = user.getNickname
    result.logoutURL = _usvc.createLogoutURL("/")
    result.owned = new JArrayList
    result.shared = new JArrayList
    for (info <- db.loadAccessibleCortices(user.getUserId)) {
      if (info.email == null || info.email == "") {
        result.owned.add(info.cortexId)
      } else {
        result.shared.add(info)
      }
    }
    result
  }

  // from DataService
  def loadAccessInfo (cortexId :String) = {
    val cortex = requireOwnedCortex(cortexId, requireUser.getUserId)
    val result = new DataService.AccessResult
    val access = db.loadCortexAccess(cortexId)
    result.publicAccess = cortex.publicAccess
    result.userAccess = new JArrayList[AccessInfo]
    result.userAccess.addAll(access.asJava)
    result
  }

  // from DataService
  def loadAccessInfo (cortexId :String, datumId :Long) = {
    val result = new DataService.AccessResult
    result.publicAccess =
      db.loadAccess(DataService.NO_USER, cortexId, datumId) getOrElse(Access.NONE)
    // TODO: result.userAccess = ...
    result
  }

  // from DataService
  def createCortex (cortexId :String) {
    val created = db.createCortex(
      cortexId, requireUser.getUserId, createRoot(cortexId), createRootContents(cortexId))
    if (!created) throw new ServiceException("e.cortex_name_in_use")
  }

  // from DataService
  def shareCortex (cortexId :String, email :String, access :Access) {
    val cortex = requireOwnedCortex(cortexId, requireUser.getUserId)
    // TODO: create pending share row
    // TODO: send email
  }

  // from DataService
  def updateCortexPublicAccess (cortexId :String, access :Access) {
    requireOwnedCortex(cortexId, requireUser.getUserId)
    db.updateCortexPublicAccess(cortexId, access)
  }

  // from DataService
  def updateCortexAccess (id :Long, access :Access) {
    if (!db.updateCortexAccess(id, requireUser.getUserId, access))
      throw new ServiceException("e.access_denied")
  }

  // from DataService
  def createDatum (cortexId :String, datum :Datum) = {
    requireWriteAccess(cortexId)
    db.createDatum(cortexId, datum)
  }

  // from DataService
  def updateDatum (cortexId :String, id :Long, field :Datum.Field, value :FieldValue) {
    requireWriteAccess(cortexId)
    db.updateDatum(cortexId, id, Seq(field -> value))
    // TODO: handle archived
  }

  // from DataService
  def updateDatum (cortexId :String, id :Long, updates :JMap[Datum.Field, FieldValue]) {
    import scalaj.collection.Imports._ // for asScala
    requireWriteAccess(cortexId)
    db.updateDatum(cortexId, id, updates.asScala.toSeq)
    // TODO: handle archived
  }

  // from DataService
  def updatePublicAccess (cortexId :String, datumId :Long, access :Access) {
    requireOwnedCortex(cortexId, requireUser.getUserId)
    db.updateDatumAccess(DataService.NO_USER, cortexId, datumId, access)
  }

  // from DataService
  def loadJournalData (cortexId :String, journalId :Long, when :Long) :Datum = {
    requireReadAccess(cortexId)
    MemoryLogic.resolveJournalDatum(cortexId, journalId, when)
  }

  // from DataService
  def deleteDatum (cortexId :String, id :Long) {
    requireWriteAccess(cortexId)
    db.deleteDatum(cortexId, id);
  }

  // from DataService
  def getUploadURL () :String = _bssvc.createUploadUrl("/upload")

  override def doUnexpectedFailure (e :Throwable) {
    e.printStackTrace(System.err)
    super.doUnexpectedFailure(e)
  }

  private def requireUser = {
    val user = _usvc.getCurrentUser
    if (user == null) throw new ServiceException("e.not_logged_in")
    else user
  }

  private def requireCortex (cortexId :String) = db.loadCortex(cortexId) match {
    case None => throw new ServiceException("e.no_such_cortex")
    case Some(cortex) => cortex
  }

  private def requireOwnedCortex (cortexId :String, userId :String) = {
    val cortex = requireCortex(cortexId)
    if (cortex.ownerId != userId) throw new ServiceException("e.access_denied")
    cortex
  }

  private def requireReadAccess (cortexId :String) {
    getAccess(cortexId) match {
      case Access.NONE => throw new ServiceException("e.access_denied")
      case _ => // peachy
    }
  }

  private def requireWriteAccess (cortexId :String) {
    getAccess(cortexId) match {
      case Access.WRITE => // peachy
      case Access.DEMO => throw new ServiceException("e.in_demo_mode")
      case _ => throw new ServiceException("e.lack_write_access")
    }
  }

  private def getAccess (cortexId :String) :Access = {
    Option(_usvc.getCurrentUser) flatMap(
      u => db.loadAccess(u.getUserId, cortexId)) getOrElse(requireCortex(cortexId).publicAccess)
  }

  private def createRoot (cortexId :String) = {
    val root = new Datum
    root.`type` = Type.PAGE
    root.meta = ""
    root.title = cortexId
    root.when = System.currentTimeMillis
    root
  }

  private def createRootContents (cortexId :String) = {
    val contents = new Datum
    contents.`type` = Type.WIKI
    contents.meta = ""
    contents.title = ""
    contents.text = "This is the main page for **" + cortexId + "**. " +
      "Click the wrench icon up above to edit it."
    contents.when = System.currentTimeMillis
    contents
  }

  private val _usvc = UserServiceFactory.getUserService
  private val _bssvc = BlobstoreServiceFactory.getBlobstoreService
}
