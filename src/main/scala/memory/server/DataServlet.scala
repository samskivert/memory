//
// $Id$

package memory.server

import com.google.gwt.user.server.rpc.RemoteServiceServlet

import memory.data.{Access, Datum, Type}
import memory.rpc.DataService

/**
 * Implements the {@link DataService}.
 */
class DataServlet extends RemoteServiceServlet with DataService
{
  // from DataService
  def createDatum (datum :Datum) {
    error("Not implemented.")
  }

  // from DataService
  def updateDatum (id :Long, parentId :java.lang.Long, access :Access, meta :String, text :String,
                   `type` :Type, when :java.lang.Long, archived :java.lang.Boolean) {
    error("Not implemented.")
  }
}
