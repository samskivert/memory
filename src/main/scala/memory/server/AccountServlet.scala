//
// $Id$

package memory.server

import scala.xml.XML
import javax.servlet.http.{HttpServlet, HttpServletRequest, HttpServletResponse}
import com.google.appengine.api.users.{User, UserService, UserServiceFactory}

/**
 * Serves up the account page.
 */
class AccountServlet extends HttpServlet
{
  override protected def doGet (req :HttpServletRequest, rsp :HttpServletResponse) {
    if (_usvc.getCurrentUser == null) {
      rsp.sendRedirect(_usvc.createLoginURL("/account"))
    } else {
      rsp.getWriter.println(Contents)
    }
  }

  private val _usvc = UserServiceFactory.getUserService

  private val Contents = """
  |<?xml version="1.0" encoding="UTF-8" ?>
  |<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
  |<html xmlns="http://www.w3.org/1999/xhtml">
  |<head><title>Memory Account</title></head>
  |<body>
  |  <div id="client"><div>Loading...</div></div>
  |  <script src="account/account.nocache.js" type="text/javascript"></script>
  |</body>
  |</html>
  """.stripMargin
}
