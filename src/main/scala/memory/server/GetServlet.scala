//
// $Id$

package memory.server

import scala.collection.JavaConversions._
import scala.xml.{Node, NodeSeq, XML}

import java.io.{PrintWriter, StringWriter}
import java.util.TimeZone
import javax.servlet.ServletConfig
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.google.appengine.api.users.{User, UserService, UserServiceFactory}
import com.google.appengine.api.blobstore.{BlobKey, BlobstoreServiceFactory}

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
      _log.info("Database initialized.")

      // create a test cortex if we're running in devmode
      if (System.getProperty("com.google.appengine.runtime.environment") == "Development") {
        if (MemoryLogic.createCortex("test", "none")) {
          db.updateCortexPublicAccess("test", Access.WRITE)
          _log.info("Created 'test' cortex. Test like the wind!")
        }
      }

    } catch {
      case e :Exception => println("Failed to init db " + e)
    }
  }

  override protected def doGet (req :HttpServletRequest, rsp :HttpServletResponse) {
    try {
      // make sure our output content type is properly set
      rsp.setContentType("text/html; charset=UTF-8")

      val rawPathInfo = req.getPathInfo
      require(rawPathInfo != null, "Missing path.")

      // the path info will be one of:
      // cortexId
      // cortexId/datumId
      // cortexId/parentId/title
      // and may be suffixed by * to indicate history mode

      val historyMode = rawPathInfo.endsWith(Datum.HISTORY_TAG)
      val pathInfo = if (historyMode) rawPathInfo.dropRight(1) else rawPathInfo
      val bits = pathInfo.split("/")
      require(bits.length >= 2, "Missing cortex name.")
      val cortexId = bits(1)

      val cortex = db.loadCortex(cortexId) getOrElse(
        throw new BadRequestException("No such cortex '" + cortexId + "'."))
      val root = db.loadDatum(cortexId, cortex.rootId)

      val datum = bits.length match {
        case 2 => root
        case 3 => db.loadDatum(cortexId, bits(2).toInt)
        case 4 => db.loadDatum(cortexId, bits(2).toInt, bits(3)) getOrElse(
          mkStub(bits(2).toInt, bits(3)))
        case _ => throw new BadRequestException("Invalid path " + rawPathInfo)
      }

      def loadParents (d :Datum) :List[Datum] = {
        if (d.parentId == 0L) Nil
        else if (d.parentId == root.id) root :: Nil
        else {
          val p = db.loadDatum(cortexId, d.parentId)
          p :: loadParents(p)
        }
      }
      def trimPath (path :List[Datum]) = {
        def trim (path :List[Datum], skip :Boolean) :List[Datum] = path match {
          case h :: t => if (skip) trim(t, h.`type` == Type.PAGE)
                         else h :: trim(t, h.`type` == Type.PAGE)
          case _ => Nil
        }
        trim(path, false)
      }
      val path = trimPath(loadParents(datum).reverse)

      // check whether this user can access this data
      val user = Option(_usvc.getCurrentUser)
      lazy val publicAccess = db.loadAccess(DataService.NO_USER, cortexId, datum.id) getOrElse(
        cortex.publicAccess)
      val access = user map(_.getUserId) match {
        case Some(userId) => db.loadAccess(userId, cortexId) getOrElse(
          db.loadAccess(userId, cortexId, datum.id) getOrElse(publicAccess))
        case None => publicAccess
      }
      // TEMP: allow media requests for anyone until we get better hierarchical access controls
      if (access == Access.NONE && datum.`type` != Type.MEDIA) user match {
        case None => throw new RedirectException(
          _usvc.createLoginURL(req.getServletPath + rawPathInfo))
        case _ => throw new BadRequestException("You lack access to this data.")
      }

      // TODO: allow the user to customize their time zone
      val now = System.currentTimeMillis + TimeZone.getTimeZone("PST8PDT").getRawOffset

      // if this is a media datum, call out to the blob store to serve it
      if (datum.`type` == Type.MEDIA) {
        rsp.setDateHeader("Last-modified", datum.when)
        _bssvc.serve(new BlobKey(datum.meta), rsp)

      } else {
        val buf = new StringWriter
        val out = new PrintWriter(buf)
        out.println(ServletUtil.htmlHeader(datum.title + " (" + cortexId + ")"))
        val pxml = <div style="display: none" id="path">{
          path.map(p => <div x:parentId={p.parentId.toString} x:title={p.title}></div>)
        }</div>
        XML.write(out, pxml, null, false, null)
        out.println
        val xml = <div style="display: none" id="root" x:cortex={cortexId}
          x:access={access.toString} x:publicAccess={publicAccess.toString}>{"\n  "}{
            toXML(MemoryLogic.resolveChildren(cortexId, now, historyMode)(datum))
          }{"\n"}</div>
        XML.write(out, xml, null, false, null)
        out.println
        out.println(GwitBits)
        out.println(ServletUtil.htmlFooter)

        // this is less efficient than just calling rsp.toWriter, but GAE doesn't do the right
        // thing with UTF8 if we do that (even if we set content-type before calling getWriter)
        rsp.getOutputStream.write(buf.toString.getBytes("UTF8"))
      }

    } catch {
      case re :RedirectException => rsp.sendRedirect(re.getMessage)
      case e :Exception =>
        if (!e.isInstanceOf[BadRequestException]) {
          _log.warning("Unexpected error", "uri", req.getRequestURI, e)
        }
        rsp.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage)
    }
  }

  private def require (condition : => Boolean, error :String) {
    if (!condition) throw new BadRequestException(error)
  }

  private def toXML (datum :Datum) :Node = {
    <def id={datum.id.toString} x:parentId={datum.parentId.toString}
         x:type={datum.`type`.toString} x:meta={datum.meta} title={datum.title}
         x:when={datum.when.toString} x:archived={datum.archived.toString}>{datum.text}
      {datum.children match {
        case null => Array[Node]()
        case children => children map(toXML)
      }}
    </def>
  }

  private def mkStub (parentId :Int, title :String) = {
    val datum = new Datum
    datum.parentId = parentId
    datum.meta = ""
    datum.title = title
    datum.`type` = Type.NONEXISTENT
    datum.when = System.currentTimeMillis
    datum
  }

  private val _usvc = UserServiceFactory.getUserService
  private val _bssvc = BlobstoreServiceFactory.getBlobstoreService
  private val _log = new Logger("GetServlet")

  private val GwitBits = """
  |<div id="client"></div>
  |<script src="/memory/memory.nocache.js" type="text/javascript"></script>
  """.stripMargin.trim
}
