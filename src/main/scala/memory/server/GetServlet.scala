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
    try {
      db.init
    } catch {
      case e => println("Failed to init db " + e)
    }
  }

  override protected def doGet (req :HttpServletRequest, rsp :HttpServletResponse) {
    val userId = 0 // TODO
    val dat = resolveChildren(db.loadRoot(userId))
    val out = rsp.getWriter
    val contents = <div id="root">{toXML(dat)}</div>
    out.println(Header)
    XML.write(out, contents, null, false, null)
    out.println(Footer)
  }

  protected def resolveChildren (rootId :Long) :Datum = resolveChildren(db.loadDatum(rootId))

  protected def resolveChildren (root :Datum) :Datum = {
    root.children = root.`type` match {
      case Type.CHECKLIST => db.loadChildren(root.id) map(resolveChildren) // TODO: archive old bits
      case Type.JOURNAL => db.loadChildren(root.id) map(resolveChildren) // TODO: just today's data
      case Type.PAGE => db.loadChildren(root.id) map(resolveChildren)
      case _ => null
    }
    root
  }

  protected def toXML (datum :Datum) :Node = {
    <def id={datum.id.toString} x:meta={datum.meta} x:type={datum.`type`.toString}
         x:when={datum.when.toString}>{datum.text}
      {datum.children match {
        case null => Array[Node]()
        case children => children map(toXML)
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
