//
// $Id$

package memory.persist

import org.junit.{After, Before, Test}
import org.junit.Assert._

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig
import com.google.appengine.tools.development.testing.LocalServiceTestHelper

import memory.data.{Access, Datum, Type}

class ObjectifyDBTest
{
  val db = objectify.ObjectifyDB
  val helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())

  @Before def beforeAll {
    helper.setUp
    db.init
  }

  @After def afterAll {
    helper.tearDown
  }

  @Test def testCRUD {
    val root = new Datum
    root.`type` = Type.PAGE
    root.meta = ""
    root.title = "Page"

    // test cortex creation and root loading
    db.createCortex("test", "user", root)
    assertEquals(Some("Page"), db.loadRoot("test") map(_.title))

    val contents = new Datum
    contents.parentId = root.id
    contents.`type` = Type.WIKI
    contents.meta = ""
    contents.title = "Howdy"
    contents.text = "Hello"
    val cid = db.createDatum("test", contents)

    // test loading by id
    assertEquals("Page", db.loadDatum("test", root.id).title)
    assertEquals("Hello", db.loadDatum("test", cid).text)

    // test bulk loading
    val data = db.loadData("test", Set(root.id, cid))
    assertEquals(2, data.size)

    // test loading children
    assertEquals("Hello", db.loadChildren("test", root.id).head.text)

    // test loading by parent + title (with matching and non-matching case)
    assertEquals(Some("Hello"), db.loadDatum("test", 1, "Howdy") map(_.text))
    assertEquals(Some("Hello"), db.loadDatum("test", 1, "howdy") map(_.text))
    assertEquals(Some("Hello"), db.loadDatum("test", 1, "HOWDY") map(_.text))
  }

  // "DB" should "support cortex access updates" in {
  //   db.updateCortexAccess("testUser", "testCortex", Access.READ)
  //   db.loadAccess("randomUser", "testCortex").getOrElse(Access.NONE) should equal(Access.NONE)
  //   db.loadAccess("testUser", "testCortex").getOrElse(Access.NONE) should equal(Access.READ)
  // }

  // "DB" should "support datum access updates" in {
  //   db.updateCortexAccess("testUser", "testCortex", 1, Access.READ)
  //   db.loadAccess("randomUser", "testCortex", 1).getOrElse(Access.NONE) should equal(Access.NONE)
  //   db.loadAccess("testUser", "testCortex", 1).getOrElse(Access.NONE) should equal(Access.READ)
  // }
}
