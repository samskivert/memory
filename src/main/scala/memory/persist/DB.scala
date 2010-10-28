//
// $Id$

package memory.persist

import memory.data.Access
import memory.data.Datum
import memory.data.Type

/**
 * Defines the interface to our persistence services.
 */
trait DB
{
  /** Initializes the database component. */
  def init :Unit

  /** Shuts down the database component. */
  def shutdown :Unit

  /** Loads the root datum for the specified user. */
  def loadRoot (userId: Long) :Datum

  /** Loads the specified datum. Throws an excepton if it does not exist. */
  def loadDatum (id :Long) :Datum

  /** Loads the children of the specified datum. */
  def loadChildren (id :Long) :Array[Datum]

  /** Loads the specified data. */
  def loadData (ids :Set[Long]) :Array[Datum]

  /** Updates the supplied fields of the specified datum. */
  def updateDatum (id :Long, parentId :Option[Long], access :Option[Access], typ :Option[Type],
                   meta :Option[String], text :Option[String], when :Option[Long]) :Unit

  /** Creates a new datum and assigns it a unique id. */
  def createDatum (datum :Datum)
}
