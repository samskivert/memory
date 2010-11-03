//
// $Id$

package memory.server

import scala.xml.{Node, NodeSeq, XML}

import javax.servlet.ServletConfig
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.google.appengine.api.users.{User, UserService, UserServiceFactory}

import memory.data.{Access, Datum, Type}
import memory.persist.DB
import memory.rpc.DataService

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

      // the path info will be one of:
      // cortexId
      // cortexId/datumId
      // cortexId/parentId/title

      val bits = req.getPathInfo.split("/")
      require(bits.length >= 2, "Missing cortex name.")
      val cortexId = bits(1)

      val root = db.loadRoot(cortexId) getOrElse(
        throw new Exception("No such cortex '" + cortexId + "'."))

      val datum = bits.length match {
        case 2 => root
        case 3 => db.loadDatum(cortexId, bits(2).toInt)
        case 4 => db.loadDatum(cortexId, bits(2).toInt, bits(3)) getOrElse(
          mkStub(bits(2).toInt, bits(3)))
        case _ => throw new Error("Invalid path " + req.getPathInfo)
      }

      val userId = Option(_usvc.getCurrentUser) map(_.getUserId) getOrElse(DataService.NO_USER)
      val access = db.loadAccess(userId, cortexId) match {
        case Access.NONE => db.loadAccess(userId, cortexId, datum.id)
        case other => other
      }
      if (access == Access.NONE) {
        if (userId == DataService.NO_USER)
          throw new RedirectException(_usvc.createLoginURL(req.getServletPath + req.getPathInfo))
        else
          throw new Exception("You lack access to this data.")
      }

      val xml = <div style="display: none" id="root" x:cortex={cortexId}>
        {toXML(resolveChildren(cortexId)(datum))}</div>
      val out = rsp.getWriter
      out.println(ServletUtil.htmlHeader(datum.title + " (" + cortexId + ")"))
      XML.write(out, xml, null, false, null)
      out.println(GwitBits)
      out.println(ServletUtil.htmlFooter)

    } catch {
      case re :RedirectException => rsp.sendRedirect(re.getMessage)
      case e => rsp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage)
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
    <def id={datum.id.toString} x:parentId={datum.parentId.toString}
         x:type={datum.`type`.toString} x:meta={datum.meta} title={datum.title}
         x:when={datum.when.toString}>{datum.text}
      {datum.children match {
        case null => Array[Node]()
        case children => children.asScala map(toXML)
      }}
    </def>
  }

  private def mkStub (parentId :Int, title :String) = {
    val datum = new Datum
    datum.parentId = parentId;
    datum.meta = ""
    datum.title = title
    datum.`type` = Type.NONEXISTENT
    datum.when = System.currentTimeMillis
    datum
  }

  private val _usvc = UserServiceFactory.getUserService

  private val Header = """
  |<?xml version="1.0" encoding="UTF-8" ?>
  |<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
  |<html xmlns="http://www.w3.org/1999/xhtml">
  |<head><title>Memory</title>
  |<meta name="viewport" content="width = device-width, user-scalable = no"/></head>
  |<body>
  """.stripMargin

  private val GwitBits = """
  |  <div id="client"></div>
  |  <script src="/memory/memory.nocache.js" type="text/javascript"></script>
  """.stripMargin
}
