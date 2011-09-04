//
// $Id$

package memory.persist
package objectify

import java.io.{InputStream, OutputStream}
import scala.collection.JavaConversions._

import com.googlecode.objectify.{Key, NotFoundException, Objectify, ObjectifyService}
import com.googlecode.objectify.annotation.Unindexed

import memory.data.{Access, AccessInfo, Cortex, Datum, FieldValue, Type}

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
      val row = obj.find(userKey(userId))
      if (row == null) {
        val nrow = new UserRow
        nrow.id = userId
        nrow.firstSession = System.currentTimeMillis
        obj.put(nrow) :Key[UserRow]
      }
    }
  }

  // from trait DB
  def createCortex (cortexId :String, ownerId :String, root :Datum, contents :Datum) :Boolean = {
    transaction { obj =>
      if (obj.find(cortexKey(cortexId)) != null) {
        return false
      }
      createDatum(cortexId, root)
      contents.parentId = root.id
      createDatum(cortexId, contents)

      val row = new CortexRow
      row.id = cortexId
      row.rootId = root.id
      row.ownerId = ownerId
      row.publicAccess = Access.NONE
      obj.put(row) :Key[CortexRow]
    }
    transaction { obj =>
      // we have to force the return type below to resolve pesky overload of put()
      obj.put(cortexAccess(ownerId, cortexId, "", Access.WRITE)) :Key[CortexAccess]
    }
    true
  }

  // from trait DB
  def loadCortex (cortexId: String) :Option[Cortex] = try {
    val obj = ObjectifyService.begin
    val cortex = obj.get(cortexKey(cortexId))
    // TEMP: handle legacy cortices that lack a publicAccess field
    if (cortex.publicAccess == null) {
      cortex.publicAccess = Access.NONE
      transaction { o2 => o2.put(cortex) :Key[CortexRow] }
    }
    Some(toJava(cortex))
  } catch {
    case e :NotFoundException => None
  }

  // from trait DB
  def loadRoot (cortexId: String) :Option[Datum] = {
    val obj = ObjectifyService.begin
    Option(obj.find(cortexKey(cortexId))) map(c => obj.get(datumKey(cortexId, c.rootId))) map(toJava)
  }

  // from trait DB
  def loadAccess (userId :String, cortexId :String) = {
    val obj = ObjectifyService.begin
    Option(obj.query(classOf[CortexAccess]).ancestor(userKey(userId)).filter(
      "cortexId", cortexId).get) map(_.access)
  }

  // from trait DB
  def loadAccess (userId :String, cortexId :String, datumId :Long) = {
    val obj = ObjectifyService.begin
    Option(obj.query(classOf[DatumAccess]).ancestor(userKey(userId)).filter(
      "cortexId", cortexId).filter("datumId", datumId).get) map(_.access)
  }

  // from trait DB
  def loadAccessibleCortices (userId :String) :Seq[AccessInfo] = {
    val obj = ObjectifyService.begin
    obj.query(classOf[CortexAccess]).ancestor(userKey(userId)).list map(toAccessInfo)
  }

  // from trait DB
  def loadCortexAccess (cortexId :String) :Seq[AccessInfo] = {
    val obj = ObjectifyService.begin
    obj.query(classOf[CortexAccess]).filter("cortexId", cortexId).list map(toAccessInfo)
  }

  // from trait DB
  def updateCortexPublicAccess (cortexId :String, access :Access) {
    transaction { obj =>
      val ctx = obj.get(cortexKey(cortexId))
      ctx.publicAccess = access
      obj.put(ctx) :Key[CortexRow]
    }
  }

  // from trait DB
  def updateCortexAccess (id :Long, callerId :String, access :Access) = try {
    transaction { obj =>
      val arow :CortexAccess = obj.get(classOf[CortexAccess], id)
      if (loadCortex(arow.cortexId).map(_.ownerId).getOrElse("") != callerId) false
      else {
        // TODO: if access == NONE, remove it
        arow.access = access
        obj.put(arow) :Key[CortexAccess]
        true
      }
    }
  } catch {
    case e :NotFoundException => {
      _log.info("Requested to update non-existent access [id=" + id + ", access=" + access + "]")
      false
    }
  }

  // from trait DB
  def updateDatumAccess (userId :String, cortexId :String, datumId :Long, access :Access) {
    transaction { obj =>
      obj.put(datumAccess(userId, cortexId, datumId, access)) :Key[DatumAccess]
    }
  }

  // from trait DB
  def loadDatum (cortexId :String, id :Long) :Datum = {
    val obj = ObjectifyService.begin
    toJava(obj.get(datumKey(cortexId, id)))
  }

  // from trait DB
  def loadDatum (cortexId :String, parentId :Long, title :String) :Option[Datum] = {
    val obj = ObjectifyService.begin
    Option(obj.query(classOf[DatumRow]).ancestor(cortexKey(cortexId)).filter(
      "parentId", parentId).filter("titleKey", title.toLowerCase).get) map(toJava)
  }

  // from trait DB
  def loadChildren (cortexId :String, id :Long) :Array[Datum] = {
    val obj = ObjectifyService.begin
    val l = obj.query(classOf[DatumRow]).ancestor(cortexKey(cortexId)).filter(
      "parentId", id).filter("archived", false).list
    (0 until l.size) map(ii => l.get(ii)) map(toJava) toArray
  }

  // from trait DB
  def loadChildren (cortexId :String, id :Long, typ :Type) :Array[Datum] = {
    val obj = ObjectifyService.begin
    val l = obj.query(classOf[DatumRow]).ancestor(cortexKey(cortexId)).filter(
      "parentId", id).filter("archived", false).filter("type", typ.toString).list
    (0 until l.size) map(ii => l.get(ii)) map(toJava) toArray
  }

  // from trait DB
  def loadData (cortexId :String, ids :Set[Long]) :Array[Datum] = {
    val obj = ObjectifyService.begin
    obj.get(ids.map(id => datumKey(cortexId, id))).values.map(toJava).toArray
  }

  // from trait DB
  def updateDatum (cortexId :String, id :Long, updates :Seq[(Datum.Field, FieldValue)]) {
    transaction { obj => 
      var datum = obj.get(datumKey(cortexId, id))
      updates.foreach { case (f, v) => updateField(datum, f, v) }
      datum.when = System.currentTimeMillis
      obj.put(datum) :Key[DatumRow]
    }
  }

  // from trait DB
  def archiveDatum (cortexId :String, id :Long) {
    transaction { obj =>
      var datum = obj.get(datumKey(cortexId, id))
      datum.archived = true
      // we don't update "when" when archiving a datum
      obj.put(datum) :Key[DatumRow]
    }
  }

  // from trait DB
  def createDatum (cortexId :String, d :Datum) = {
    transaction { obj =>
      val row = new DatumRow
      row.cortex = cortexKey(cortexId)
      row.parentId = d.parentId
      row.`type` = d.`type`
      row.meta = d.meta
      row.titleKey = d.title.toLowerCase
      row.title = d.title
      row.text = d.text
      row.when = d.when
      // row.archived = false

      val key :Key[DatumRow] = obj.put(row)
      d.id = key.getId
      d.id
    }
  }

  // from trait DB
  def deleteDatum (cortexId :String, id :Long) {
    transaction { obj =>
      obj.delete(datumKey(cortexId, id))
    }
  }

  // from trait DB
  def dump (out :OutputStream) {
    // // TODO
    // import java.io._
    // val dout = new DataOutputStream(new BufferedOutputStream(out))
    // transaction { obj =>
    //   // first dump the cortex rows
    //   val iter = obj.query(classOf[CortexRow])
    //   while (iter.hasNext) {
    //     val cr = iter.next
    //     System.out.println("CR: " + cr)
    //     dout.writeUTF8("CortexRow")
    //     dout.writeUTF8(cr.id);
    //     dout.writeLong(cr.rootId);
    //     dout.writeUTF8(cr.ownerId);
    //     dout.writeUTF8(cr.publicAccess.toString);
    //   }
    // }
  }

  // from trait DB
  def undump (in :InputStream) {
    // TODO
  }

  private def cortexAccess (userId :String, cortexId :String, email :String, access :Access) = {
    val acc = new CortexAccess
    acc.userId = userKey(userId)
    acc.cortexId = cortexId
    acc.email = email
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

  private def updateField (datum :DatumRow, field :Datum.Field, value :FieldValue) {
    field match {
      case Datum.Field.PARENT_ID =>
        datum.parentId = value.asInstanceOf[FieldValue.LongValue].value
      case Datum.Field.TYPE =>
        datum.`type` = value.asInstanceOf[FieldValue.TypeValue].value
      case Datum.Field.META =>
        datum.meta = value.asInstanceOf[FieldValue.StringValue].value
      case Datum.Field.TITLE => {
        datum.title = value.asInstanceOf[FieldValue.StringValue].value
        datum.titleKey = datum.title.toLowerCase
      }
      case Datum.Field.TEXT =>
        datum.text = value.asInstanceOf[FieldValue.StringValue].value
      case Datum.Field.WHEN =>
        datum.when = value.asInstanceOf[FieldValue.LongValue].value
      case Datum.Field.ARCHIVED =>
        datum.archived = value.asInstanceOf[FieldValue.BooleanValue].value
      case _ => throw new IllegalArgumentException("Unknown field " + field)
    }
  }

  private def toJava (row :CortexRow) = {
    val cortex = new Cortex
    cortex.id = row.id
    cortex.rootId = row.rootId
    cortex.ownerId = row.ownerId
    cortex.publicAccess = row.publicAccess
    cortex
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
    datum.archived = row.archived
    datum
  }

  private def userKey (userId :String) = new Key(classOf[UserRow], userId)
  private def cortexKey (cortexId :String) = new Key(classOf[CortexRow], cortexId)
  private def datumKey (cortexId :String, datumId :Long) =
    new Key(cortexKey(cortexId), classOf[DatumRow], datumId)
  private def toAccessInfo (ca :CortexAccess) =
    new AccessInfo(ca.id.longValue, ca.cortexId, ca.email, ca.access)

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
  private val _log = java.util.logging.Logger.getLogger("objdb")
}
