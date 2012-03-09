//
// $Id$

package memory.server

import org.junit.Test
import org.junit.Assert.{assertEquals, assertTrue}

class LoggerTest extends Logger("test")
{
  @Test def testLogger {
    assertEquals("foo", format("foo", Nil))

    // check handling of simple arguments
    assertEquals("foo [bar=baz]", format("foo", Seq("bar", "baz")))
    assertEquals("foo [bar=baz, bif=1]", format("foo", Seq("bar", "baz", "bif", 1)))

    // check handling of null keys and values
    assertEquals("foo [bar=baz, null=1, c=null]",
                 format("foo", Seq("bar", "baz", null, 1, "c", null)))

    // check handling of exception as value
    assertEquals("foo [bar=java.lang.Throwable: Ack!]",
                 format("foo", Seq("bar", new Throwable("Ack!"))))

    // check identification of exception as final arg
    val exn = new Throwable("Ack!")
    assertTrue(null == getexn("foo", Seq("bar", exn)))
    assertTrue(exn == getexn("foo", Seq("bar", "baz", exn)))
    assertTrue(exn == getexn(exn, Nil))
  }
}
