//
// $Id$

package memory.persist.squeryl

import memory.data.Access
import memory.data.Datum
import memory.data.Type
import memory.persist.DB

/**
 * Implements our persistence services using Squeryl and H2.
 */
class SquerylDB extends DB
{
  def loadRoot (userId: Long) :Datum = error("Not implemented.")

  def loadDatum (id :Long) :Datum = error("Not implemented.")

  def loadChildren (id :Long) :Seq[Datum] = error("Not implemented.")

  def loadData (ids :Set[Long]) :Seq[Datum] = error("Not implemented.")

  def updateDatum (id :Long, parentId :Option[Long], access :Option[Access], typ :Option[Type],
                   meta :Option[String], text :Option[String], when :Option[Long]) {
    error("Not implemented.")
  }

  def createDatum (datum :Datum) {
    error("Not implemented.")
  }
}
