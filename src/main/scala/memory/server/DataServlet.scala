//
// $Id$

package memory.server

import scalaj.collection.Imports._

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import com.google.appengine.api.users.{User, UserService, UserServiceFactory}

import memory.data.{Access, Datum, Type}
import memory.persist.DB
import memory.rpc.{DataService, ServiceException}

/**
 * Implements the {@link DataService}.
 */
class DataServlet extends RemoteServiceServlet with DataService
{
  // TODO: configure this based on servlet config?
  val db :DB = memory.persist.squeryl.SquerylDB

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
  def createCortex (cortexId :String) {
    db.createCortex(cortexId, requireUser.getUserId,
                    createRoot(cortexId), createRootContents(cortexId))
  }

  // from DataService
  def createDatum (datum :Datum) = {
    val userId = 0 // TODO
    val parent = db.loadDatum(datum.parentId)
    // TODO: check access using parent
    db.createDatum(datum)
  }

  // from DataService
  def updateDatum (id :Long, parentId :java.lang.Long, `type` :Type, meta :String,
                   title :String, text :String, when :java.lang.Long, archived :java.lang.Boolean) {
    // TODO: check access
    db.updateDatum(id, Option(parentId) map(_.longValue), Option(`type`), Option(meta),
                   Option(title), Option(text), Option(when) map(_.longValue))
    // TODO: handle archived
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
