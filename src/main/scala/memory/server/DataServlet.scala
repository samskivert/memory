//
// $Id$

package memory.server

import scala.collection.JavaConversions._
import scala.collection.mutable.{Seq => MSeq}

import java.util.{Map => JMap, List => JList, ArrayList => JArrayList}
import com.google.gwt.user.server.rpc.RemoteServiceServlet

import com.google.appengine.api.users.{User, UserServiceFactory}
import com.google.appengine.api.blobstore.{BlobstoreServiceFactory}

import memory.data.{Access, AccessInfo, Datum, FieldValue, Type}
import memory.persist.DB
import memory.rpc.{DataService, ServiceException}

/**
 * Implements the {@link DataService}.
 */
class DataServlet extends RemoteServiceServlet with DataService
{
  // TODO: configure this based on servlet config?
  val db :DB = memory.persist.objectify.ObjectifyDB

  // from DataService
  def loadAccountInfo = {
    val user = _usvc.getCurrentUser
    ServiceException.require(user != null, "e.not_logged_in")

    val result = new DataService.AccountResult
    result.userId = user.getUserId
    result.nickname = user.getNickname
    result.logoutURL = _usvc.createLogoutURL("/")
    result.owned = new JArrayList
    result.shared = new JArrayList
    for (info <- db.loadAccessibleCortices(user.getUserId)) {
      if (info.email == null || info.email == "") {
        result.owned.add(info.cortexId)
      } else {
        result.shared.add(info)
      }
    }
    result
  }

  // from DataService
  def loadAccessInfo (cortexId :String) = {
    val cortex = requireOwnedCortex(cortexId, requireUser.getUserId)
    val result = new DataService.AccessResult
    val access = db.loadCortexAccess(cortexId)
    result.publicAccess = cortex.publicAccess
    result.userAccess = new JArrayList[AccessInfo]
    result.userAccess.addAll(access)
    result
  }

  // from DataService
  def loadAccessInfo (cortexId :String, datumId :Long) = {
    val result = new DataService.AccessResult
    result.publicAccess =
      db.loadAccess(DataService.NO_USER, cortexId, datumId) getOrElse(Access.NONE)
    // TODO: result.userAccess = ...
    result
  }

  // from DataService
  def createCortex (cortexId :String) {
    val created = MemoryLogic.createCortex(cortexId, requireUser.getUserId)
    ServiceException.require(created, "e.cortex_name_in_use")
  }

  // from DataService
  def shareCortex (cortexId :String, email :String, access :Access) {
    val user = requireUser
    val cortex = requireOwnedCortex(cortexId, user.getUserId)
    // create a persistent record to track the share request
    val token = md5hex(cortexId + email + access + user.getUserId + System.currentTimeMillis)
    val shareId = db.createShareRequest(token, cortexId, access)
    // send email to the sharee
    try {
      val body = SHARE_TMPL.replaceAll("TOKEN", token).replaceAll("SENDER", user.getEmail)
      sendEmail(user.getEmail, email, SHARE_SUBJECT, body)
    } catch {
      // clean up after ourselves if we fail to send the email
      case e => {
        db.deleteShareRequest(shareId)
        throw new ServiceException(e.getMessage)
      }
    }
  }

  // from DataService
  def updateCortexPublicAccess (cortexId :String, access :Access) {
    requireOwnedCortex(cortexId, requireUser.getUserId)
    db.updateCortexPublicAccess(cortexId, access)
  }

  // from DataService
  def updateCortexAccess (id :Long, access :Access) {
    val updated = db.updateCortexAccess(id, requireUser.getUserId, access)
    ServiceException.require(updated, "e.access_denied")
  }

  // from DataService
  def getShareInfo (token :String) = {
    val (user, copt) = (requireUser, db.loadShareInfo(token))
    val info = new DataService.ShareInfo
    info.cortex = copt.getOrElse(throw new ServiceException("e.no_such_token"))
    info.nickname = user.getNickname
    info.logoutURL = _usvc.createLogoutURL("/")
    info
  }

  // from DataService
  def acceptShareRequest (token :String) {
    val user = requireUser
    val accepted = db.acceptShareRequest(token, user.getUserId, user.getEmail)
    ServiceException.require(accepted, "e.no_such_token")
  }

  // from DataService
  def createDatum (cortexId :String, datum :Datum) = {
    requireWriteAccess(cortexId)
    db.createDatum(cortexId, datum)
  }

  // from DataService
  def updateDatum (cortexId :String, id :Long, field :Datum.Field, value :FieldValue) {
    requireWriteAccess(cortexId)
    db.updateDatum(cortexId, id, Seq(field -> value))
    // TODO: handle archived
  }

  // from DataService
  def updateDatum (cortexId :String, id :Long, updates :JMap[Datum.Field, FieldValue]) {
    requireWriteAccess(cortexId)
    db.updateDatum(cortexId, id, updates.toSeq)
    // TODO: handle archived
  }

  // from DataService
  def updatePublicAccess (cortexId :String, datumId :Long, access :Access) {
    requireOwnedCortex(cortexId, requireUser.getUserId)
    db.updateDatumAccess(DataService.NO_USER, cortexId, datumId, access)
  }

  // from DataService
  def loadJournalData (cortexId :String, journalId :Long, when :Long) :Datum = {
    requireReadAccess(cortexId)
    MemoryLogic.resolveJournalDatum(cortexId, journalId, when)
  }

  // from DataService
  def deleteDatum (cortexId :String, id :Long) {
    requireWriteAccess(cortexId)
    db.deleteDatum(cortexId, id);
  }

  // from DataService
  def getUploadURL () :String = _bssvc.createUploadUrl("/upload")

  override def doUnexpectedFailure (e :Throwable) {
    e.printStackTrace(System.err)
    super.doUnexpectedFailure(e)
  }

  private def requireUser = {
    val user = _usvc.getCurrentUser
    ServiceException.require(user != null, "e.not_logged_in")
    user
  }

  private def requireCortex (cortexId :String) = db.loadCortex(cortexId) match {
    case None => throw new ServiceException("e.no_such_cortex")
    case Some(cortex) => cortex
  }

  private def requireOwnedCortex (cortexId :String, userId :String) = {
    val cortex = requireCortex(cortexId)
    ServiceException.require(cortex.ownerId == userId, "e.access_denied")
    cortex
  }

  private def requireReadAccess (cortexId :String) {
    ServiceException.require(getAccess(cortexId) != Access.NONE, "e.access_denied")
  }

  private def requireWriteAccess (cortexId :String) {
    getAccess(cortexId) match {
      case Access.WRITE => // peachy
      case Access.DEMO => throw new ServiceException("e.in_demo_mode")
      case _ => throw new ServiceException("e.lack_write_access")
    }
  }

  private def getAccess (cortexId :String) :Access = {
    Option(_usvc.getCurrentUser) flatMap(
      u => db.loadAccess(u.getUserId, cortexId)) getOrElse(requireCortex(cortexId).publicAccess)
  }

  private def sendEmail (sender :String, recip :String, subject :String, body :String) {
    import javax.mail._
    import javax.mail.internet._
    val session = Session.getDefaultInstance(new java.util.Properties, null)
    val msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(sender))
    msg.addRecipient(Message.RecipientType.TO, new InternetAddress(recip))
    msg.setSubject(subject)
    msg.setText(body)
    Transport.send(msg)
  }

  private def md5hex (text :String) =
    java.security.MessageDigest.getInstance("MD5").digest(
      text.getBytes("UTF-8")) map("%02x" format _) mkString

  private val _usvc = UserServiceFactory.getUserService
  private val _bssvc = BlobstoreServiceFactory.getBlobstoreService

  private val SHARE_SUBJECT = "Share this cortex!"
  private val SHARE_TMPL = """
    SENDER has shared a cortex with you on sparecortex.com.

    Click the link below to access it:

    http://www.sparecortex.com/account#STOKEN

    If SENDER didn't tell you what this is all about, feel free to just delete this email.

    Thanks!

    - The Spare Cortex Management
  """
}
