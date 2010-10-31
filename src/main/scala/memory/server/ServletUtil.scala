//
// $Id$

package memory.server

/**
 * Helper functions for servlet stuff.
 */
object ServletUtil
{
  def htmlHeader (title :String) = """
  |<?xml version="1.0" encoding="UTF-8" ?>
  |<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
  |<html xmlns="http://www.w3.org/1999/xhtml">
  |<head>
  |  <title>TITLE</title>
  |  <meta name="viewport" content="width = device-width, user-scalable = no"/>
  |</head>
  |<body>
  """.stripMargin replace("TITLE", title)

  def htmlFooter = """
  |</body>
  |</html>
  """.stripMargin
}

// used to redireect servlet requesters elsewhere
class RedirectException (url :String) extends Exception(url)
