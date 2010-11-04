//
// $Id$

package memory.server

import scalaj.collection.Imports._

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import com.google.appengine.api.users.{User, UserService, UserServiceFactory}

import memory.data.{Access, Datum, FieldValue, Type}
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
    result.nickname = user.getNickname
    result.cortexen = new java.util.HashMap[Access, java.util.List[String]]
    for ((k, v) <- db.loadCortexAccess(user.getUserId) groupBy(_._1)) {
      val list = new java.util.ArrayList[String]
      v foreach { p => list.add(p._2) }
      result.cortexen.put(k, list)
    }
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
  def updateDatum (cortexId :String, id :Long, field1 :Datum.Field, value1 :FieldValue,
                   field2 :Datum.Field, value2 :FieldValue) {
    requireWriteAccess(cortexId)
    db.updateDatum(cortexId, id, Seq(field1 -> value1, field2 -> value2))
    // TODO: handle archived
  }

  // from DataService
  def updateDatum (cortexId :String, id :Long, field1 :Datum.Field, value1 :FieldValue,
                   field2 :Datum.Field, value2 :FieldValue,
                   field3 :Datum.Field, value3 :FieldValue) {
    requireWriteAccess(cortexId)
    db.updateDatum(cortexId, id, Seq(field1 -> value1, field2 -> value2, field3 -> value3))
    // TODO: handle archived
  }

  // from DataService
  def updateAccess (userId :String, cortexId :String, datumId :Long, access :Access) {
    val updater = requireUser.getUserId
    if (updater != "root" && updater != db.loadOwner(cortexId))
      throw new ServiceException("e.access_denied")
    db.updateAccess(userId, cortexId, datumId, access)
  }

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
    val userId = requireUser.getUserId
    if (userId != "root" && db.loadAccess(userId, cortexId).getOrElse(Access.NONE) != Access.WRITE)
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
}
