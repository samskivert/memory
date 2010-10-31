//
// $Id$

package memory.persist

import org.scalatest.{FlatSpec, BeforeAndAfterAll}
import org.scalatest.matchers.ShouldMatchers

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

import memory.data.{Datum, Type}

/**
 * Tests the Objectify based database.
 */
class ObjectifyDBSpec extends FlatSpec with BeforeAndAfterAll with ShouldMatchers
{
  val db = objectify.ObjectifyDB
  val helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())

  override def beforeAll {
    helper.setUp
    db.init
  }

  override def afterAll {
    helper.tearDown
  }

  "DB" should "support basic CRUD" in {
    val root = new Datum
    root.`type` = Type.PAGE
    root.meta = ""
    root.title = "Page"

    val contents = new Datum
    contents.`type` = Type.WIKI
    contents.meta = ""
    contents.title = ""
    contents.text = "Hello"

    // test cortex creation and root loading
    db.createCortex("test", "user", root, contents)
    db.loadRoot("test") map(_.title) should equal(Some("Page"))

    // test loading by id
    db.loadDatum("test", 1).title should equal("Page")
    db.loadDatum("test", 2).text should equal("Hello")

    // test bulk loading
    val data = db.loadData("test", Set(1, 2))
    data.size should equal(2)

    // test loading children
    db.loadChildren("test", root.id).head.text should equal("Hello")
  }
}
