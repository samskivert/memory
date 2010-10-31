//
// $Id$

package memory.server

import scala.xml.{Node, NodeSeq, XML}

import javax.servlet.ServletConfig
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.google.appengine.api.users.{User, UserService, UserServiceFactory}

import memory.data.{Access, Datum, Type}
import memory.persist.DB

/**
 * Serves up REST data.
 */
class GetServlet extends HttpServlet
{
  // TODO: configure this based on servlet config?
  val db :DB = memory.persist.objectify.ObjectifyDB

  override def init (config :ServletConfig) {
    try {
      db.init
    } catch {
      case e => println("Failed to init db " + e)
    }
  }

  override protected def doGet (req :HttpServletRequest, rsp :HttpServletResponse) {
    try {
      require(req.getPathInfo != null, "Missing path.")

      val bits = req.getPathInfo.split("/")
      require(bits.length >= 2, "Missing cortex name.")
      val cortexId = bits(1)

      val user = _usvc.getCurrentUser
      val access = db.loadAccess(if (user == null) "" else user.getUserId, cortexId)
      require(access != Access.NONE, "You lack access to '" + cortexId + "'.")

      db.loadRoot(cortexId) match {
        case None => throw new Exception("No such cortex '" + cortexId + "'.")
        case Some(root) => {
          val contents = <div id="root" x:cortex={cortexId}>
            {toXML(resolveChildren(cortexId)(root))}</div>
          val out = rsp.getWriter
          out.println(Header)
          XML.write(out, contents, null, false, null)
          out.println(Footer)
        }
      }

    } catch {
      case e => 
      rsp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage)
    }
  }

  private def require (condition : => Boolean, error :String) {
    if (!condition) throw new Exception(error)
  }

  private def resolveChildren (cortexId :String, rootId :Long) :Datum =
    resolveChildren(cortexId)(db.loadDatum(cortexId, rootId))

  private def resolveChildren (cortexId :String)(root :Datum) :Datum = {
    root.children = root.`type` match {
      case Type.LIST => resolveChildList(cortexId, root.id) // TODO: archive old bits
      case Type.CHECKLIST => resolveChildList(cortexId, root.id) // TODO: archive old bits
      case Type.JOURNAL => resolveChildList(cortexId, root.id) // TODO: just today's data
      case Type.PAGE => resolveChildList(cortexId, root.id)
      case _ => null
    }
    root
  }

  private def resolveChildList (cortexId :String, id :Long) =
    java.util.Arrays.asList(db.loadChildren(cortexId, id) map(resolveChildren(cortexId)) :_*)

  private def toXML (datum :Datum) :Node = {
    import scalaj.collection.Imports._
    <def id={datum.id.toString} x:type={datum.`type`.toString} x:meta={datum.meta}
         title={datum.title} x:when={datum.when.toString}>{datum.text}
      {datum.children match {
        case null => Array[Node]()
        case children => children.asScala map(toXML)
      }}
    </def>
  }

  private val _usvc = UserServiceFactory.getUserService

  private val Header = """
  |<?xml version="1.0" encoding="UTF-8" ?>
  |<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
  |<html xmlns="http://www.w3.org/1999/xhtml">
  |<head><title>Memory</title></head>
  |<body>
  """.stripMargin

  private val Footer = """
  |  <div id="client" style="min-height: 600px"></div>
  |  <iframe id="__gwt_historyFrame" style="width:0;height:0;border:0"></iframe>
  |  <script src="/memory/memory.nocache.js" type="text/javascript"></script>
  |</body>
  |</html>
  """.stripMargin
}
