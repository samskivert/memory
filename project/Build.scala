import sbt._
import Keys._
import com.github.siasia.WebPlugin.webSettings
import net.thunderklaus.GwtPlugin._

object MemoryBuild extends Build {
  val asyncGen = TaskKey[Unit]("async-gen", "Generates GWT service Async classes")
  private def asyncGenTask =
    (streams, sourceDirectory, classDirectory in Compile, dependencyClasspath in Compile) map {
      (s, sourceDir, classes, depCP) => {
        val cp = (classes +: depCP.map(_.data)) map(_.toURI.toURL)
        val loader = java.net.URLClassLoader.newInstance(cp.toArray)
        val genner = new com.samskivert.asyncgen.AsyncGenerator(loader, null) {
          override def fail (message :String, cause :Throwable) =
            new RuntimeException(message, cause)
        }
        val sources = (sourceDir ** "*Service.java").get
        s.log.debug("Generating async interfaces for: " + sources.mkString(", "))
        sources foreach { genner.processInterface(_) }
      }
    }

  val i18nSync = TaskKey[Unit]("i18n-sync", "Generates i18n Messages interfaces from properties")
  private def i18nSyncTask =
    (streams, javaSource in Compile) map {
      (s, sourceDir) => {
        val props = (sourceDir ** "*Messages.properties").get
        s.log.debug("Generating i18n interfaces for: " + props.mkString(", "))
        props foreach { f => com.threerings.gwt.tools.I18nSync.processFile(sourceDir, f) }
      }
    }

  val gaeVers = "1.6.3"
  val extSettings = Defaults.defaultSettings ++ webSettings ++ gwtSettings

  val memory = Project("memory", file("."), settings = extSettings ++ Seq(
    organization  := "com.samskivert",
    name          := "memory",
    version       := "1.1-SNAPSHOT",
    scalaVersion  := "2.9.1",
    scalacOptions ++= Seq("-unchecked", "-deprecation"),

    gwtVersion    := "2.4.0",
    gaeSdkPath    := Some(Path.userHome + "/ops/appengine-java-sdk-" + gaeVers),
    gwtTemporaryPath <<= (target) { (target) => target / "webapp" },
    javacOptions  ++= Seq("-Xlint", "-Xlint:-serial"),

    // give GWT some memory juices
    javaOptions in Gwt ++= Seq("-mx512M"),

    resolvers ++= Seq(
      "Local Maven Repository" at Path.userHome.asURL + ".m2/repository",
      "Objectify repo" at "http://objectify-appengine.googlecode.com/svn/maven"
    ),

    autoScalaLibrary := true, // GWT plugin turns this off for some reason
    libraryDependencies ++= Seq(
      // we only need these for the GWT build, so we use "provided"
      "com.threerings" % "gwt-utils" % "1.5" % "provided",
      "com.allen-sauer.gwt.dnd" % "gwt-dnd" % "3.1.2" % "provided",

      // appengine depends
      "com.google.appengine" % "appengine-api-1.0-sdk" % gaeVers,
      "com.google.appengine" % "appengine-testing" % gaeVers % "test",
      "com.google.appengine" % "appengine-api-stubs" % gaeVers % "test",
      "com.google.appengine" % "appengine-api-labs" % gaeVers % "test",

      // database depends
      "com.googlecode.objectify" % "objectify" % "2.2.1",
      "javax.persistence" % "persistence-api" % "1.0",

      // needed for xsbt-web-plugin
      "org.mortbay.jetty" % "jetty" % "6.1.22" % "container",

      // test dependencies
      "org.scalatest" % "scalatest" % "1.2" % "test"
    ),

    asyncGen <<= asyncGenTask,
    i18nSync <<= i18nSyncTask
  ))
}
