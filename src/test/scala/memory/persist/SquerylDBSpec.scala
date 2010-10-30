//
// $Id$

package memory.persist

import java.util.Date
import java.sql.DriverManager

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import org.squeryl.{Session, SessionFactory}
import org.squeryl.adapters.H2Adapter
import org.squeryl.PrimitiveTypeMode._

import memory.data.{Datum, Type}

/**
 * Tests the squeryl database subsystem.
 */
class SquerylDBSpec extends FlatSpec with ShouldMatchers
{
  def testSession = {
    Class.forName("org.h2.Driver")
    val url = "jdbc:h2:mem:test;ignorecase=true"
    val s = Session.create(DriverManager.getConnection(url, "sa", ""), new H2Adapter)
    // s.setLogger(println) // for great debugging!
    s
  }

  val db = squeryl.SquerylDB

  "DB" should "support basic CRUD" in {
    SessionFactory.concreteFactory = Some(() => testSession)
    transaction {
      db.checkCreate

      val root = new Datum
      root.`type` = Type.PAGE
      root.meta = ""
      root.title = "Page"

      val contents = new Datum
      contents.`type` = Type.WIKI
      contents.meta = ""
      contents.title = ""
      contents.text = "Hello"

      db.createCortex("test", "user", root, contents)
      db.loadRoot("test") map(_.title) should equal(Some("Page"))
    }
  }
}
