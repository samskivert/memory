//
// $Id$

package memory.server

import scala.xml.{Node, NodeSeq, XML}

import javax.servlet.ServletConfig
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import memory.data.{Datum, Type}
import memory.persist.DB

/**
 * Serves up REST data.
 */
class GetServlet extends HttpServlet
{
  // TODO: configure this based on servlet config?
  val db :DB = memory.persist.squeryl.SquerylDB

  override def init (config :ServletConfig) {
    println("Initializing database services!")
    try {
      db.init
    } catch {
      case e => println("Failed to init db " + e)
    }
  }

  override protected def doGet (req :HttpServletRequest, rsp :HttpServletResponse) {
    val userId = 0 // TODO
    val dat = db.loadRoot(userId)
    val out = rsp.getWriter

    out.println("<html><head><title>Root</title></head><body>")
    XML.write(out, toXML(dat), null, false, null)
    out.println("</body></html>")
  }

  protected def loadChildData (datum :Datum) :Seq[Datum] = datum.`type` match {
    case Type.CHECKLIST => db.loadChildren(datum.id) // TODO: archive old bits
    case Type.JOURNAL => db.loadChildren(datum.id) // TODO: just today's data
    case Type.PAGE => db.loadChildren(datum.id)
    case _ => Seq()
  }

  protected def toXML (datum :Datum) :Node = {
    <def id={datum.id.toString} x:meta={datum.meta} x:type={datum.`type`.toString}
         x:when={datum.when.toString}>{datum.text}
      {loadChildData(datum) map(toXML)}
    </def>
  }
}
