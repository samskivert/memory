//
// $Id$

package memory.server

import scala.collection.JavaConversions._

import java.util.logging.Level
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}

import com.google.appengine.api.blobstore.BlobstoreServiceFactory

import memory.data.{Datum, Type}
import memory.persist.DB
import memory.rpc.ServiceException

/**
 * Handles media uploads.
 */
class UploadServlet extends HttpServlet
{
  // TODO: configure this based on servlet config?
  val db :DB = memory.persist.objectify.ObjectifyDB

  override protected def doPost (req :HttpServletRequest, rsp :HttpServletResponse) {
    val blobs = _bssvc.getUploadedBlobs(req)
    try {
      val media = blobs.get("media")
      if (media == null) {
        throw new ServiceException("Missing media for upload [bcount=" + blobs.size + "]")
      } else {
        // val url = _bssvc.getServingUrl(media)
        val cortexId = requireParam(req, "cortexId")
        val parentId = requireParam(req, "parentId")
        val name = requireParam(req, "name")

        val parent = db.loadDatum(cortexId, parentId.toLong)
        // TODO: require write access to cortex or parent datum

        // create a new datum to reference this blob
        val bdatum = new Datum
        bdatum.parentId = parent.id
        bdatum.`type` = Type.MEDIA
        bdatum.meta = media.getKeyString
        bdatum.title = name
        bdatum.text = ""
        bdatum.when = System.currentTimeMillis
        db.createDatum(cortexId, bdatum)

        // report the new datum id to the uploader; we have to do this wacky redirect back to
        // ourselves because AppEngine requires that the response from an upload be a redirect
        rsp.sendRedirect("/upload?newId=" + bdatum.id)
      }

    } catch {
      case t :Throwable => {
        t match {
          case se :ServiceException => _log.warning(se.getMessage)
          case _ => _log.warning(t.getMessage, t) // log the whole stack trace
        }
        // delete any uploaded blobs
        blobs.values.map(bkey => _bssvc.delete(bkey))
        rsp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, t.getMessage)
      }
    }
  }

  override protected def doGet (req :HttpServletRequest, rsp :HttpServletResponse) {
    rsp.setHeader("Content-Type", "text/html") // prevent fiddling with our output
    rsp.getWriter.println(req.getParameter("newId"))
  }

  private def isBlank (text :String) = (text == null) || (text.trim.length == 0)
  private def requireParam (req :HttpServletRequest, name :String) = {
    val value = req.getParameter(name)
    if (isBlank(value)) throw new ServiceException("Missing parameter '" + name + "'")
    value
  }

  private val _bssvc = BlobstoreServiceFactory.getBlobstoreService
  private val _log = new Logger("UploadServlet")
}
