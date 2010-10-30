//
// $Id$

package memory.persist
package squeryl

import java.io.File
import java.sql.DriverManager

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter
import org.squeryl.annotations.Column
import org.squeryl.dsl.CompositeKey2
import org.squeryl.{KeyedEntity, Schema, Session, SessionFactory}

import memory.data.{Access, Datum, Type}

/**
 * Implements our persistence services using Squeryl and H2.
 */
object SquerylDB extends Schema with DB
{
  /** Provides cortices. */
  val cortexen = table[CortexRow]
  // TODO: uncomment this and KeyedEntity[String] below when Max fixes Squeryl
  // on(cortexen)(c => declare(
  //   c.id is (primaryKey)
  // ))

  /** Provides datums. */
  val data = table[DatumRow]

  /** Provices cortex access control data. */
  val cortexAccess = table[CortexAccess]

  /** Provices datum access control data. */
  val datumAccess = table[DatumAccess]

  /** Maps {@link Type} elements to an Int that can be used in the DB. */
  val typeToCode = Map(
    Type.WIKI -> 1,
    Type.HTML -> 2,
    Type.EMBED -> 3,
    Type.LIST -> 4,
    Type.CHECKLIST -> 5,
    Type.JOURNAL -> 6,
    Type.PAGE -> 7
  ) // these mappings must never change (but can be extended)

  /** Maps an Int code back to a {@link Type}. */
  val codeToType = typeToCode map { case(x, y) => (y, x) }

  /** Maps {@link Access} elements to an Int that can be used in the DB. */
  val accessToCode = Map(
    Access.NONE -> 0,
    Access.READ -> 1,
    Access.WRITE -> 2
  ) // these mappings must never change (but can be extended)

  /** Maps an Int code back to a {@link Access}. */
  val codeToAccess = accessToCode map { case(x, y) => (y, x) }

  def init {
    // initialize the H2 database
    Class.forName("org.h2.Driver")
    // TODO: make configurable
    // val dburl = "jdbc:h2:database"
    val dburl = "jdbc:h2:mem:database;DB_CLOSE_DELAY=-1" // TEMP while testing GAE
    SessionFactory.concreteFactory = Some(() => {
      // TODO: use connection pools as Squeryl creates and closes a connection on every query
      val sess = Session.create(DriverManager.getConnection(dburl, "sa", ""), new H2Adapter)
      // sess.setLogger(println)
      sess
    })

    // make sure our schema is created
    checkCreate
  }

  def checkCreate {
    try {
      loadAccess("", "")
    } catch {
      case e => {
        if (e.getMessage.indexOf("Table \"CORTEXACCESS\" not found") == 0) {
          e.printStackTrace // TODO
        } else transaction {
          create // create the initial database
        }
      }
    }
  }

  def shutdown {
    // nada for now?
  }

  def createCortex (cortexId :String, ownerId :String, root :Datum, contents :Datum) {
    transaction {
      createDatum(root)
      contents.parentId = root.id
      createDatum(contents)
      cortexen.insert(CortexRow(cortexId, root.id))
      cortexAccess.insert(CortexAccess(cortexId, ownerId, accessToCode(Access.WRITE)))
    }
  }

  def loadRoot (cortexId: String) :Option[Datum] = transaction {
    cortexen.where(_.id === cortexId).headOption flatMap(c => data.lookup(c.rootId)) map(toJava)
  }

  def loadAccess (cortexId :String, userId :String) = transaction {
    from(cortexAccess)(ca =>
      where(ca.id === (cortexId, userId))
      select(ca.access)).headOption map(codeToAccess) getOrElse(Access.NONE)
  }

  def loadAccess (datumId :Long, userId :String) = transaction {
    from(datumAccess)(da =>
      where(da.id === (datumId, userId))
      select(da.access)).headOption map(codeToAccess) getOrElse(Access.NONE)
  }

  def loadCortexAccess (userId :String) :Seq[(Access,String)] = transaction {
    from(cortexAccess)(ca => where(ca.userId === userId) select(ca.access, ca.cortexId)) map(
      pr => (codeToAccess(pr._1), pr._2)) toSeq
  }

  def updateAccess (cortexId :String, userId :String, access :Access) {
    transaction {
      if (update(cortexAccess)(ca =>
        where(ca.id === (cortexId, userId))
        set(ca.access := accessToCode(access))) == 0) {
          cortexAccess.insert(CortexAccess(cortexId, userId, accessToCode(access)))
       }
    }
  }

  def updateAccess (datumId :Long, userId :String, access :Access) {
    transaction {
      if (update(datumAccess)(da =>
        where(da.id === (datumId, userId))
        set(da.access := accessToCode(access))) == 0) {
          datumAccess.insert(DatumAccess(datumId, userId, accessToCode(access)))
       }
    }
  }

  def loadDatum (id :Long) = transaction {
    data.lookup(id) map(toJava) getOrElse(null)
  }

  def loadChildren (id :Long) = transaction {
    data.where(d => d.parentId === id) map(toJava) toArray
  }

  def loadData (ids :Set[Long]) = transaction {
    data.where(d => d.id in ids) map(toJava) toArray
  }

  def updateDatum (id :Long, parentId :Option[Long], typ :Option[Type], meta :Option[String],
                   title :Option[String], text :Option[String], when :Option[Long]) {
    transaction {
      // don't know of a good way to construct a set() with optional elements
      parentId foreach(value => data.update(d => where(d.id === id) set(d.parentId := value)))
      typ foreach(
        value => data.update(d => where(d.id === id) set(d.`type` := typeToCode(value))))
      meta foreach(value => data.update(d => where(d.id === id) set(d.meta := value)))
      title foreach(value => data.update(d => where(d.id === id) set(d.title := value)))
      text foreach(value => data.update(d => where(d.id === id) set(d.text := Some(value))))
      when foreach(value => data.update(d => where(d.id === id) set(d.when := value)))
    }
  }

  def createDatum (datum :Datum) = transaction {
    val row = fromJava(datum)
    data.insert(row)
    datum.id = row.id
    row.id
  }

  protected def toJava (row :DatumRow) = {
    val datum = new Datum
    datum.id = row.id
    datum.parentId = row.parentId
    datum.`type` = codeToType(row.`type`)
    datum.meta = row.meta
    datum.title = row.title
    datum.text = row.text.getOrElse(null)
    datum.when = row.when
    datum
  }

  protected def fromJava (datum :Datum) =
    DatumRow(datum.parentId, typeToCode(datum.`type`), datum.meta,
             datum.title, Option(datum.text), datum.when)
}

/** Models the data for a single datum as a database row. */
case class DatumRow (
  /** The id of this datum's parent, or 0 if it is a root datum. */
  parentId :Long,
  /** Indicates the type of this datum. */
  `type` :Int,
  /** Metadata for this datum. */
  @Column(length=1024) meta :String,
  /** The title of this datum. */
  @Column(length=256) title :String, // TODO: fix scalac: @Column(length=Datum.MAX_TITLE_LENGTH)
  /** The primary contents of this datum. */
  @Column(length=65536) text :Option[String],
  /** A timestamp associated with this datum (usually when it was last modified). */
  when :Long
) extends KeyedEntity[Long] {
  /** A unique identifier for this datum (1 or higher). */
  val id :Long = 0L

  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, 0, "", "", Some(""), 0L)

  // override def toString = "[id=" + id + ", type=" + `type` + "]"
}

/** Contains the data for a Cortex. */
case class CortexRow (
  /** The id of this cortex. */
  id :String,
  /** The id of the root datum for this cortex. */
  rootId :Long
) /*extends KeyedEntity[String]*/ {
  /** Zero args ctor for use when unserializing. */
  def this () = this("", 0L)
}

/** Maintains access control mappings for cortices. */
case class CortexAccess (
  /** The cortex in question. */
  cortexId :String,
  /** The user in question. */
  userId :String,
  /** The user's access to the cortex. */
  access :Int
) extends KeyedEntity[CompositeKey2[String,String]] {
  /** Defines our composite primary key. */
  def id = compositeKey(cortexId, userId)
  /** Zero args ctor for use when unserializing. */
  def this () = this("", "", 0)
}

/** Maintains access control mappings for data. */
case class DatumAccess (
  /** The datum in question. */
  datumId :Long,
  /** The user in question. */
  userId :String,
  /** The user's access to the datum. */
  access :Int
) extends KeyedEntity[CompositeKey2[Long,String]] {
  /** Defines our composite primary key. */
  def id = compositeKey(datumId, userId)
  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, "", 0)
}
