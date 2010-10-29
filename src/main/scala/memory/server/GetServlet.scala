//
// $Id$

package memory.server

import scala.xml.{Node, NodeSeq, XML}

import javax.servlet.ServletConfig
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import memory.data.{Access, Datum, Type}
import memory.persist.DB

/**
 * Serves up REST data.
 */
class GetServlet extends HttpServlet
{
  // TODO: configure this based on servlet config?
  val db :DB = memory.persist.squeryl.SquerylDB

  override def init (config :ServletConfig) {
    try {
      db.init
    } catch {
      case e => println("Failed to init db " + e)
    }
  }

  override protected def doGet (req :HttpServletRequest, rsp :HttpServletResponse) {
    val userId = 0 // TODO
    val dat = resolveChildren(loadUserRoot(userId))
    val out = rsp.getWriter
    val contents = <div id="root">{toXML(dat)}</div>
    out.println(Header)
    XML.write(out, contents, null, false, null)
    out.println(Footer)
  }

  protected def loadUserRoot (userId :Long) = {
    db.loadRoot(userId) match {
      case None => {
        val root = createUserPage(userId)
        db.createDatum(root)
        db.createDatum(createUserPageContents(root))
        root
      }
      case Some(root) => root
    }
  }

  protected def createUserPage (userId :Long) = {
    val root = new Datum
    root.access = Access.GNONE_WNONE
    root.`type` = Type.PAGE
    root.meta = ""
    root.title = "Welcome to Memory"
    root.when = System.currentTimeMillis
    root
  }

  protected def createUserPageContents (root :Datum) = {
    val contents = new Datum
    contents.parentId = root.id
    contents.access = Access.GNONE_WNONE
    contents.`type` = Type.MARKDOWN
    contents.meta = ""
    contents.title = ""
    contents.text = "This is your first page. Click the edit button to the right to edit it."
    contents.when = System.currentTimeMillis
    contents
  }

  protected def resolveChildren (rootId :Long) :Datum = resolveChildren(db.loadDatum(rootId))

  protected def resolveChildren (root :Datum) :Datum = {
    root.children = root.`type` match {
      case Type.LIST => resolveChildList(root.id) // TODO: archive old bits
      case Type.CHECKLIST => resolveChildList(root.id) // TODO: archive old bits
      case Type.JOURNAL => resolveChildList(root.id) // TODO: just today's data
      case Type.PAGE => resolveChildList(root.id)
      case _ => null
    }
    root
  }

  protected def resolveChildList (id :Long) =
    java.util.Arrays.asList(db.loadChildren(id) map(resolveChildren) :_*)

  protected def toXML (datum :Datum) :Node = {
    import scalaj.collection.Imports._
    <def id={datum.id.toString} x:access={datum.access.toString} x:type={datum.`type`.toString}
         x:meta={datum.meta} title={datum.title} x:when={datum.when.toString}>{datum.text}
      {datum.children match {
        case null => Array[Node]()
        case children => children.asScala map(toXML)
      }}
    </def>
  }

  val Header = """
  |<?xml version="1.0" encoding="UTF-8" ?>
  |<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
  |<html xmlns="http://www.w3.org/1999/xhtml">
  |<head><title>Memory</title></head>
  |<body>
  """.stripMargin

  val Footer = """
  |  <div id="client" style="min-height: 600px"></div>
  |  <iframe id="__gwt_historyFrame" style="width:0;height:0;border:0"></iframe>
  |  <script src="memory.nocache.js" type="text/javascript"></script>
  |</body>
  |</html>
  """.stripMargin
}
