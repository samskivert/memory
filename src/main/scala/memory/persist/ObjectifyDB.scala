//
// $Id$

package memory.persist
package objectify

import java.io.{InputStream, OutputStream}
import java.util.{List => JList}
import scala.collection.JavaConversions._

import com.googlecode.objectify.{Key, NotFoundException, ObjectifyService}

import memory.data.{Access, AccessInfo, Cortex, Datum, FieldValue, Type}
import memory.server.Logger

/**
 * Implements the database via Objectify (which builds on GAE Data Store).
 */
object ObjectifyDB extends DB
{
  import ObjectifyService.{ofy, run, register}

  val entities = List[Class[_]](
    classOf[UserRow], classOf[DatumRow], classOf[CortexRow], classOf[ShareRow],
    classOf[CortexAccess], classOf[DatumAccess])

  // from trait DB
  def init (extra :Runnable) {
    run { () =>
      entities foreach register
      extra.run()
    }
  }

  // from trait DB
  def shutdown {
    // nada for now
  }

  // from trait DB
  def noteUser (userId :String) {
    ofy.transact { () =>
      val row = find(userKey(userId))
      if (row == null) {
        val nrow = new UserRow
        nrow.id = userId
        nrow.firstSession = System.currentTimeMillis
        put(nrow)
      }
    }
  }

  // from trait DB
  def createCortex (cortexId :String, ownerId :String, root :Datum) :Boolean = {
    ofy.transact { () =>
      if (find(cortexKey(cortexId)) != null) {
        return false
      }
      createDatum(cortexId, root)

      val row = new CortexRow
      row.id = cortexId
      row.rootId = root.id
      row.ownerId = ownerId
      row.publicAccess = Access.NONE
      put(row)
    }
    ofy.transact { () => put(cortexAccess(ownerId, cortexId, "", Access.WRITE)) }
    true
  }

  // from trait DB
  def deleteCortex (cortexId :String) {
    // load up all of the access records for this cortex
    val cortexAccess = query[CortexAccess].filter("cortexId", cortexId).list

    // load the keys for all data in this cortex
    val dataKeys = query[DatumRow].ancestor(cortexKey(cortexId)).keys

    // load the data access rows (TODO: this is heinously inefficient, but there are only 24 data
    // acess rows in existence at the moment)
    val dataAccess :JList[DatumAccess] = query[DatumAccess].list.filter(_.id.startsWith(cortexId+":"))

    // first delete the bits with CortexRow ancestor
    ofy.transact { () =>
      ofy.delete.keys(dataKeys)
      ofy.delete.key(cortexKey(cortexId))
    }
    // next delete the bits with UserRow ancestor
    ofy.transact { () =>
      ofy.delete.entities(dataAccess)
      ofy.delete.entities(cortexAccess)
    }
  }

  // from trait DB
  def loadCortex (cortexId: String) :Option[Cortex] = try {
    val cortex = get(cortexKey(cortexId))
    // TEMP: handle legacy cortices that lack a publicAccess field
    if (cortex.publicAccess == null) {
      cortex.publicAccess = Access.NONE
      ofy.transact { () => put(cortex) }
    }
    Some(toJava(cortex))
  } catch {
    case e :NotFoundException => None
  }

  // from trait DB
  def loadRoot (cortexId: String) :Option[Datum] = {
    Option(get(cortexKey(cortexId))).map(c => get(datumKey(cortexId, c.rootId))).map(toJava)
  }

  // from trait DB
  def loadAccess (userId :String, cortexId :String) = Option(query[CortexAccess].
    ancestor(userKey(userId)).filter("cortexId", cortexId).first.now) map(_.access)

  // from trait DB
  def loadAccess (userId :String, cortexId :String, datumId :Long) = {
    val key = Key.create(userKey(userId), classOf[DatumAccess], cortexId + ":" + datumId)
    try Some(get(key).access)
    catch {
      case e :NotFoundException => None
    }
  }

  // from trait DB
  def loadAccessibleCortices (userId :String) :Seq[AccessInfo] =
    query[CortexAccess].ancestor(userKey(userId)).list.map(toAccessInfo)

  // from trait DB
  def loadCortexAccess (cortexId :String) :Seq[AccessInfo] =
    query[CortexAccess].filter("cortexId", cortexId).list map(toAccessInfo)

  // from trait DB
  def updateCortexPublicAccess (cortexId :String, access :Access) {
    ofy.transact { () =>
      val ctx = get(cortexKey(cortexId))
      ctx.publicAccess = access
      put(ctx)
    }
  }

  // from trait DB
  def updateCortexAccess (id :Long, callerId :String, access :Access) = try {
    ofy.transact { () =>
      val arow :CortexAccess = get(Key.create(classOf[CortexAccess], id))
      if (loadCortex(arow.cortexId).map(_.ownerId).getOrElse("") != callerId) false
      else {
        // TODO: if access == NONE, remove it
        arow.access = access
        put(arow)
        true
      }
    }
  } catch {
    case e :NotFoundException =>
      _log.info("Requested to update non-existent access", "id", id, "access", access) ; false
  }

  // from trait DB
  def updateDatumAccess (userId :String, cortexId :String, datumId :Long, access :Access) {
    val da = datumAccess(userId, cortexId, datumId, access)
    ofy.transact { () => put(da) }
  }

  // from trait DB
  def loadDatum (cortexId :String, id :Long) :Datum = toJava(get(datumKey(cortexId, id)))

  // from trait DB
  def loadDatum (cortexId :String, parentId :Long, title :String) :Option[Datum] =
    Option(query[DatumRow].ancestor(cortexKey(cortexId)).filter("parentId", parentId).
      filter("titleKey", title.toLowerCase).first.now) map(toJava)

  // from trait DB
  def loadChildren (cortexId :String, id :Long, includeArchived :Boolean) :Array[Datum] = {
    val l1 = query[DatumRow].ancestor(cortexKey(cortexId)).filter("parentId", id)
    val l2 = (if (includeArchived) l1 else l1.filter("archived", false)).list
    (0 until l2.size) map(ii => l2.get(ii)) map(toJava) toArray
  }

  // from trait DB
  def loadChildren (cortexId :String, id :Long, typ :Type) :Array[Datum] = {
    val l = query[DatumRow].ancestor(cortexKey(cortexId)).filter(
      "parentId", id).filter("archived", false).filter("type", typ.toString).list
    (0 until l.size) map(ii => l.get(ii)) map(toJava) toArray
  }

  // from trait DB
  def loadData (cortexId :String, ids :Set[Long]) :Array[Datum] =
    get(ids.map(id => datumKey(cortexId, id))).values.map(toJava).toArray

  // from trait DB
  def updateDatum (cortexId :String, id :Long, updates :Seq[(Datum.Field, FieldValue)]) {
    ofy.transact { () =>
      var datum = get(datumKey(cortexId, id))
      updates.foreach { case (f, v) => updateField(datum, f, v) }
      datum.when = System.currentTimeMillis
      put(datum)
    }
  }

  // from trait DB
  def archiveDatum (cortexId :String, id :Long) {
    ofy.transact { () =>
      var datum = get(datumKey(cortexId, id))
      datum.archived = true
      // we don't update "when" when archiving a datum
      put(datum)
    }
  }

  // from trait DB
  def createDatum (cortexId :String, d :Datum) = {
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

    val key = ofy.transact { () => put(row) }
    d.id = key.getId
    d.id
  }

  // from trait DB
  def deleteDatum (cortexId :String, id :Long) {
    ofy.transact { () => ofy.delete.key(datumKey(cortexId, id)) }
  }

  // from trait DB
  def cloneChildren (fromCortexId :String, fromId :Long, toCortexId :String, toId :Long) {
    // duplicate the child data, and compute a list of (oldId, newId) for the duped children
    val dups = query[DatumRow].ancestor(
      cortexKey(fromCortexId)).filter("parentId", fromId).map { fromChild =>
        val toChild = fromChild.clone
        toChild.cortex = cortexKey(toCortexId)
        toChild.id = null
        toChild.parentId = toId
        val key = ofy.transact { () => put(toChild) }
        (fromChild.id.longValue, key.getId)
      }

    // recursively copy the children's children
    dups.foreach { case (oldId, newId) => cloneChildren(fromCortexId, oldId, toCortexId, newId) }

    // update the metadata for the parent of the cloned children
    val dupMap = dups.toMap
    val parent = get(datumKey(toCortexId, toId))
    val meta = parent.meta.split(";").filter(_ != "").map(_.split("=").toSeq)
    try {
      val conv = meta.map {
        case Seq(k,v) => Seq(k, k match {
          // manually rewrite the child ids in breakN and order metadata
          case ("break1" | "break2") => dupMap.getOrElse(v.toLong, 0L).toString
          case "order" => v.split(",").map(_.toLong).filter(dupMap.keySet).map(dupMap).mkString(",")
          case _ => v
        })
        case v => _log.warning("Weird metadata value: " + v, "parent", parent.title); v
      }
      parent.meta = conv.map(_.mkString("=")).mkString(";")
      ofy.transact { () => put(parent) }
    } catch {
      case e :Exception =>
        _log.warning("Failed to convert metadata", "meta", parent.meta, "mmap", meta, e)
    }
  }

  // from trait DB
  def createShareRequest (token :String, cortexId :String, access :Access) = {
    val row = new ShareRow
    row.token = token
    row.cortexId = cortexId
    row.access = access
    ofy.transact { () => put(row) } getId
  }

  // from trait DB
  def loadShareInfo (token :String) :Option[String] = mapShareInfo(token) { _.cortexId }

  // from trait DB
  def acceptShareRequest (token :String, userId :String, email :String) = mapShareInfo(token) {
    info =>
      deleteShareRequest(info.id)
      ofy.transact { () =>
        // delete any previous cortex access rows for this user+cortex
        query[CortexAccess].ancestor(userKey(userId)).filter(
          "cortexId", info.cortexId).list foreach ofy.delete.entity
        // store their new access
        put(cortexAccess(userId, info.cortexId, email, info.access))
      }
  } isDefined

  // from trait DB
  def deleteShareRequest (shareId :Long) {
    ofy.transact { () => ofy.delete.key(Key.create(classOf[ShareRow], shareId)) }
  }

  // from trait DB
  def dump (out :OutputStream) {
    // // TODO
    // import java.io._
    // val dout = new DataOutputStream(new BufferedOutputStream(out))
    // ofy.transact { () =>
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
    acc.id = cortexId + ":" + datumId
    acc.access = access
    acc
  }

  private def mapShareInfo[T] (token :String)(f :(ShareRow => T)) :Option[T] =
    query[ShareRow].filter("token", token).list.headOption.map(f)

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

  private def get[T] (key :Key[T]) :T = ofy.load.key(key).safe
  private def get[T] (keys :Iterable[Key[T]]) = ofy.load.keys(asJavaIterable(keys))
  private def find[T] (key :Key[T]) :T = ofy.load.key(key).now
  private def put[T] (entity :T) :Key[T] = ofy.save.entity(entity).now
  private def query[T](implicit m :ClassManifest[T]) =
    ofy.load.`type`(m.erasure.asInstanceOf[Class[T]])

  private def userKey (userId :String) = Key.create(classOf[UserRow], userId)
  private def cortexKey (cortexId :String) = Key.create(classOf[CortexRow], cortexId)
  private def datumKey (cortexId :String, datumId :Long) =
    Key.create(cortexKey(cortexId), classOf[DatumRow], datumId)
  private def toAccessInfo (ca :CortexAccess) =
    new AccessInfo(ca.id.longValue, ca.cortexId, ca.email, ca.access)

  private val _log = new Logger("objdb")
}
