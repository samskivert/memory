//
// $Id$

package memory.server

import java.util.logging.Level

/**
 * Provides a more ergonomic interface to Java's logging API. Use like so:
 * {{{
 * val log = new Logger("example")
 * log.info("Frobbing bizulator", "count", 25, "monkeys", monkeyCount)
 * try { ... } catch {
 *   // when exn is passed as last arg (with no key) its stack trace is logged
 *   case err => log.warning("Oh the huge manatees!", "count", 25, err)
 * }
 * }}}
 */
class Logger (name :String)
{
  /** Logs a debug message.
   * @param msg the message to be logged (will be `toString`ed).
   * @param args a list of key/value pairs and an optional final `Throwable`.
   */
  def debug (msg :Any, args :Any*) = doLog(Level.FINE, msg, args)

  /** Logs an info message.
   * @param msg the message to be logged (will be `toString`ed).
   * @param args a list of key/value pairs and an optional final `Throwable`.
   */
  def info (msg :Any, args :Any*) = doLog(Level.INFO, msg, args)

  /** Logs a warning message.
   * @param msg the message to be logged (will be `toString`ed).
   * @param args a list of key/value pairs and an optional final `Throwable`.
   */
  def warning (msg :Any, args :Any*) = doLog(Level.WARNING, msg, args)

  /** Logs an error message.
   * @param msg the message to be logged (will be `toString`ed).
   * @param args a list of key/value pairs and an optional final `Throwable`.
   */
  def error (msg :Any, args :Any*) = doLog(Level.SEVERE, msg, args)

  protected def doLog (level :Level, msgobj :Any, args :Seq[Any]) {
    try {
      if (_impl.isLoggable(level))
        _impl.log(level, format(msgobj, args), getexn(msgobj, args))
    } catch {
      case e => _impl.log(Level.WARNING, "Failure generating log message", e)
    }
  }

  protected def format (msgobj :Any, args :Seq[Any]) =
    String.valueOf(msgobj) + (if (args.length < 2) ""
                              else args.grouped(2).map(_.mkString("=")).mkString(" [", ", ", "]"))

  protected def getexn (msgobj :Any, args :Seq[Any]) = msgobj match {
    case ex :Throwable => ex
    case _ if (args.size % 2 == 1) => args.last match {
      case ex :Throwable => ex
      case _ => null
    }
    case _ => null
  }

  private[this] val _impl = java.util.logging.Logger.getLogger(name)
}
