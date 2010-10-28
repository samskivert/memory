//
// $Id$

package memory.persist
package squeryl

import java.io.File
import java.sql.DriverManager

import org.squeryl.PrimitiveTypeMode._
import org.squeryl.adapters.H2Adapter
import org.squeryl.{KeyedEntity, Schema, Session, SessionFactory}

import memory.data.{Access, Datum, Type}

/**
 * Implements our persistence services using Squeryl and H2.
 */
object SquerylDB extends Schema with DB
{
  /** Provides access to datums. */
  val data = table[DatumRow]

  /** Maps {@link Type} elements to an Int that can be used in the DB. */
  val typeToCode = Map(
    Type.MARKDOWN -> 1,
    Type.HTML -> 2,
    Type.EMBED -> 3,
    Type.CHECKLIST -> 4,
    Type.JOURNAL -> 5,
    Type.PAGE -> 6
  ) // these mappings must never change (but can be extended)

  /** Maps an Int code back to a {@link Type}. */
  val codeToType = typeToCode map { case(x, y) => (y, x) }

  /** Maps {@link Access} elements to an Int that can be used in the DB. */
  val accessToCode = Map(
    Access.GNONE_WNONE -> 0,
    Access.GREAD_WNONE -> 1,
    Access.GWRITE_WNONE -> 2,
    Access.GREAD_WREAD -> 3,
    Access.GWRITE_WREAD -> 4,
    Access.GWRITE_WWRITE -> 5
  ) // these mappings must never change (but can be extended)

  /** Maps an Int code back to a {@link Access}. */
  val codeToAccess = accessToCode map { case(x, y) => (y, x) }

  def init {
    // initialize the H2 database
    Class.forName("org.h2.Driver")
    val dburl = "jdbc:h2:database" // TODO: make configurable
    SessionFactory.concreteFactory = Some(() => {
      // TODO: use connection pools as Squeryl creates and closes a connection on every query
      val sess = Session.create(DriverManager.getConnection(dburl, "sa", ""), new H2Adapter)
      // sess.setLogger(println)
      sess
    })

    // make sure the root datum exists
    try {
      loadRoot(0)
    } catch {
      case e => {
        if (e.getMessage.indexOf("Table \"DATUMROW\" not found") == 0) {
          e.printStackTrace // TODO
        } else transaction {
          // create the initial database and insert a root datum
          create
          data.insert(DatumRow(0, 0, 1, "", "Welcome to Memory.", System.currentTimeMillis))
        }
      }
    }
  }

  def shutdown {
    // nada for now?
  }

  def loadRoot (userId: Long) :Datum = transaction {
    // in our single-user test setup, the first datum is the root
    data.lookup(1L) map(toJava) get
  }

  def loadDatum (id :Long) :Datum = transaction {
    data.lookup(id) map(toJava) getOrElse(null)
  }

  def loadChildren (id :Long) :Array[Datum] = transaction {
    data.where(d => d.parentId === id) map(toJava) toArray
  }

  def loadData (ids :Set[Long]) :Array[Datum] = transaction {
    data.where(d => d.id in ids) map(toJava) toArray
  }

  def updateDatum (id :Long, parentId :Option[Long], access :Option[Access], typ :Option[Type],
                   meta :Option[String], text :Option[String], when :Option[Long]) {
    error("Not implemented.")
  }

  def createDatum (datum :Datum) {
    error("Not implemented.")
  }

  protected def toJava (row :DatumRow) :Datum = {
    val datum = new Datum
    datum.id = row.id
    datum.parentId = row.parentId
    datum.access = codeToAccess(row.access)
    datum.`type` = codeToType(row.`type`)
    datum.meta = row.meta
    datum.text = row.text
    datum.when = row.when
    datum
  }
}

/**
 * Models the data for a single datum as a database row.
 */
case class DatumRow (
  /** The id of this datum's parent, or 0 if it is a root datum. */
  parentId :Long,
  /** Indicates the access controls for this datum. */
  access :Int,
  /** Indicates the type of this datum. */
  `type` :Int,
  /** Metadata for this datum. */
  meta :String,
  /** The primary contents of this datum. */
  text :String,
  /** A timestamp associated with this datum (usually when it was last modified). */
  when :Long
) extends KeyedEntity[Long] {
  /** A unique identifier for this datum (1 or higher). */
  val id :Long = 0L

  /** Zero args ctor for use when unserializing. */
  def this () = this(0L, 0, 0, "", "", 0L)

  override def toString = "[id=" + id + ", type=" + `type` + "]"
}
