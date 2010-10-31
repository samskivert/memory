//
// $Id$

package memory.persist
package objectify

import scalaj.collection.Imports._

import com.googlecode.objectify.{Key, Objectify, ObjectifyService}
import com.googlecode.objectify.annotation.Unindexed

import memory.data.{Access, Datum, Type}

/**
 * Implements the database via Objectify (which builds on GAE Data Store).
 */
object ObjectifyDB extends DB
{
  // from trait DB
  def init {
    ObjectifyService.register(classOf[UserRow])
    ObjectifyService.register(classOf[DatumRow])
    ObjectifyService.register(classOf[CortexRow])
    ObjectifyService.register(classOf[CortexAccess])
    ObjectifyService.register(classOf[DatumAccess])
  }

  // from trait DB
  def shutdown {
    // nada for now
  }

  // from trait DB
  def noteUser (userId :String) {
    transaction { obj =>
      val row = obj.get(userKey(userId))
      if (row == null) {
        val nrow = new UserRow
        nrow.id = userId
        nrow.firstSession = System.currentTimeMillis
        obj.put(nrow) :Key[UserRow]
      }
    }
  }

  // from trait DB
  def createCortex (cortexId :String, ownerId :String, root :Datum, contents :Datum) {
    transaction { obj =>
      createDatum(cortexId, root)
      contents.parentId = root.id
      createDatum(cortexId, contents)
      obj.put(cortexRow(cortexId, root.id)) :Key[CortexRow]
    }
    transaction { obj =>
      // we have to force the return type below to resolve pesky overload of put()
      obj.put(cortexAccess(cortexId, ownerId, Access.WRITE)) :Key[CortexAccess]
    }
  }

  // from trait DB
  def loadRoot (cortexId: String) :Option[Datum] = transaction { obj =>
    Option(obj.get(cortexKey(cortexId))) map(c => obj.get(datumKey(cortexId, c.rootId))) map(toJava)
  }

  // from trait DB
  def loadAccess (userId :String, cortexId :String) = transaction { obj =>
    Option(obj.query(classOf[CortexAccess]).ancestor(
      userKey(userId)).filter("cortexId", cortexId).get) map(_.access) getOrElse(Access.NONE)
  }

  // from trait DB
  def loadAccess (userId :String, datumId :Long) = {
    error("unimplemented")
  }

  // from trait DB
  def loadCortexAccess (userId :String) :Seq[(Access,String)] = transaction { obj =>
    obj.query(classOf[CortexAccess]).ancestor(userKey(userId)).list.asScala map(
      ca => (ca.access, ca.cortexId))
  }

  // from trait DB
  def updateAccess (userId :String, cortexId :String, access :Access) :Unit = {
    error("unimplemented")
  }

  // from trait DB
  def updateAccess (userId :String, datumId :Long, access :Access) {
    error("unimplemented")
  }

  // from trait DB
  def loadDatum (cortexId :String, id :Long) :Datum = transaction { obj =>
    Option(obj.get(datumKey(cortexId, id))) map(toJava) getOrElse(error("No such datum " + id))
  }

  // from trait DB
  def loadChildren (cortexId :String, id :Long) :Array[Datum] = transaction { obj =>
    val l = obj.query(classOf[DatumRow]).ancestor(cortexKey(cortexId)).filter("parentId", id).list
    (0 until l.size) map(ii => l.get(ii)) map(toJava) toArray
  }

  // from trait DB
  def loadData (cortexId :String, ids :Set[Long]) :Array[Datum] = transaction { obj =>
    obj.get(ids.map(id => datumKey(cortexId, id)).asJava).values.asScala.map(toJava).toArray
  }

  // from trait DB
  def updateDatum (cortexId :String, id :Long, parentId :Option[Long], typ :Option[Type],
                   meta :Option[String], title :Option[String], text :Option[String],
                   when :Option[Long]) {
    transaction { obj => 
      var datum = obj.get(datumKey(cortexId, id))
      parentId foreach(datum.parentId = _)
      typ foreach(datum.`type` = _)
      meta foreach(datum.meta = _)
      title foreach(datum.title = _)
      text foreach(datum.text = _)
      when foreach(datum.when = _)
      obj.put(datum) :Key[DatumRow]
    }
  }

  // from trait DB
  def createDatum (cortexId :String, d :Datum) = {
    transaction { obj =>
      val key :Key[DatumRow] = obj.put(
        datumRow(cortexId, d.parentId, d.`type`, d.meta, d.title, d.text, d.when))
      d.id = key.getId
      println("Created datum in cortex " + cortexId + ": " + d.id)
      d.id
    }
  }

  private def cortexRow (id :String, rootId :Long) = {
    val row = new CortexRow
    row.id = id
    row.rootId = rootId
    row
  }

  private def datumRow (cortexId :String, parentId :Long, type_ :Type,
                        meta :String, title :String, text :String, when :Long) = {
    val row = new DatumRow
    row.cortex = cortexKey(cortexId)
    row.`type` = type_
    row.meta = meta
    row.title = title
    row.text = text
    row.when = when
    row
  }

  private def cortexAccess (userId :String, cortexId :String, access :Access) = {
    val acc = new CortexAccess
    acc.userId = userKey(userId)
    acc.cortexId = cortexId
    acc.access = access
    acc
  }

  private def datumAccess (userId :String, cortexId :String, datumId :Long, access :Access) = {
    val acc = new DatumAccess
    acc.userId = userKey(userId)
    acc.cortexId = cortexId
    acc.datumId = datumId
    acc.access = access
    acc
  }

  private def toJava (row :DatumRow) = {
    val datum = new Datum
    datum.id = row.id.longValue
    datum.parentId = row.parentId
    datum.`type` = row.`type`
    datum.meta = row.meta
    datum.title = row.title
    datum.text = row.text
    datum.when = row.when
    datum
  }

  private def userKey (userId :String) = new Key(classOf[UserRow], userId)
  private def cortexKey (cortexId :String) = new Key(classOf[CortexRow], cortexId)
  private def datumKey (cortexId :String, datumId :Long) =
    new Key(cortexKey(cortexId), classOf[DatumRow], datumId)

  private def transaction[T] (action :Objectify => T) = {
    if (_txobj != null) {
      action(_txobj) // execute action in "nested" transaction
    } else {
      _txobj = ObjectifyService.beginTransaction
      try {
        val result = action(_txobj)
        _txobj.getTxn.commit
        result
      } finally {
        if (_txobj.getTxn.isActive) _txobj.getTxn.rollback
        _txobj = null
      }
    }
  }

  private[this] var _txobj :Objectify = _
}
