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
      val out = rsp.getWriter
      out.println(ServletUtil.htmlHeader("Spare Cortex Account"))
      out.println(GwitBits)
      out.println(ServletUtil.htmlFooter)
    }
  }

  private val _usvc = UserServiceFactory.getUserService

  private val GwitBits = """
  |  <div id="client"><div>Loading...</div></div>
  |  <script src="account/account.nocache.js" type="text/javascript"></script>
  """.stripMargin
}
