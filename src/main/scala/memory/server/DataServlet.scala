//
// $Id$

package memory.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet

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
  def createDatum (datum :Datum) = {
    val userId = 0 // TODO
    val parent = db.loadDatum(datum.parentId)
    // TODO: check access using parent
    db.createDatum(datum)
  }

  // from DataService
  def updateDatum (id :Long, parentId :java.lang.Long, access :Access, `type` :Type,
                   meta :String, title :String, text :String,
                   when :java.lang.Long, archived :java.lang.Boolean) {
    // TODO: check access
    db.updateDatum(id, Option(parentId) map(_.longValue), Option(access), Option(`type`),
                   Option(meta), Option(title), Option(text), Option(when) map(_.longValue))
    // TODO: handle archived
  }

  override def doUnexpectedFailure (e :Throwable) {
    e.printStackTrace(System.err)
    super.doUnexpectedFailure(e)
  }
}
