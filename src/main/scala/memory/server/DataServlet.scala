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
  def loadAccessInfo (cortexId :String) :JList[AccessInfo] = {
    if (requireUser.getUserId != db.loadOwner(cortexId))
      throw new ServiceException("e.access_denied")
    val result = new JArrayList[AccessInfo]
    result.addAll(db.loadCortexAccess(cortexId).asJava)
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
    if (requireUser.getUserId != db.loadOwner(cortexId))
      throw new ServiceException("e.access_denied")
    // TODO: create pending share row
    // TODO: send email
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
    if (requireUser.getUserId != db.loadOwner(cortexId))
      throw new ServiceException("e.access_denied")
    db.updateDatumAccess(DataService.NO_USER, cortexId, datumId, access)
  }

  // from DataService
  def loadJournalData (cortexId :String, journalId :Long, when :Long) :Datum = {
    requireWriteAccess(cortexId) // TODO: allow read-only access (disabling creation)?
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

  private def requireWriteAccess (cortexId :String) {
    if (db.loadAccess(requireUser.getUserId, cortexId).getOrElse(Access.NONE) != Access.WRITE)
      throw new ServiceException("e.lack_write_access")
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
      "Click the edit button to the right to edit it."
    contents.when = System.currentTimeMillis
    contents
  }

  private val _usvc = UserServiceFactory.getUserService
  private val _bssvc = BlobstoreServiceFactory.getBlobstoreService
}
