import sbt._
import Keys._
import com.github.siasia.WebPlugin.webSettings
import net.thunderklaus.GwtPlugin._

object MemoryBuild extends Build {
  val gaeVers = "1.5.3"

  val TooolsConfig = config("tools") extend(Compile)
  val asyncGen = TaskKey[Unit]("async-gen", "Generates GWT service Async classes")

  val extSettings = Defaults.defaultSettings ++ webSettings ++ gwtSettings

  val memory = Project(
    "memory", file("."), settings = extSettings ++ Seq(
      organization     := "com.samskivert",
      name             := "memory",
      version          := "1.1-SNAPSHOT",
      scalaVersion     := "2.9.0-1",
      scalacOptions    ++= Seq("-unchecked", "-deprecation"),

      gwtVersion       := "2.3.0",
      gaeSdkPath       := Some(Path.userHome + "/ops/appengine-java-sdk-1.5.3"),
      javacOptions     ++= Seq("-Xlint", "-Xlint:-serial"),

      resolvers ++= Seq(
        "Local Maven Repository" at Path.userHome.asURL + ".m2/repository",
        "Objectify repo" at "http://objectify-appengine.googlecode.com/svn/maven"
      ),

      autoScalaLibrary := true, // GWT plugin turns this off for some reason
      libraryDependencies ++= Seq(
        // we only need these for the GWT build, so we use "provided"
        "com.threerings" % "gwt-utils" % "1.3" % "provided",
        "allen_sauer" % "gwt-dnd" % "3.1.1" % "provided",

        // appengine depends
        "com.google.appengine" % "appengine-api-1.0-sdk" % gaeVers,
        "com.google.appengine" % "appengine-testing" % gaeVers % "test",
        "com.google.appengine" % "appengine-api-stubs" % gaeVers % "test",
        "com.google.appengine" % "appengine-api-labs" % gaeVers % "test",

        // database depends
        "com.googlecode.objectify" % "objectify" % "2.2.1",
        "javax.persistence" % "persistence-api" % "1.0",

        // test dependencies
        "org.scalatest" % "scalatest" % "1.2" % "test"

        // tool dependencies
        // "com.samskivert" % "gwt-asyncgen" % "1.0" % "tools"
      ),

      asyncGen <<= asyncGenTask
    )
  )

  private def asyncGenTask = (streams, sourceDirectory) map {
    (s, sourceDir) => {
      val sources = (sourceDir ** "*Service.java").get.map(_.getPath)
      s.log.info("Generating async interfaces for: " + sources.mkString(", "))
      // com.samskivert.asyncgen.AsyncGenTool.main(sources.toArray)
    }
  }

  // // used to obtain the path for a specific dependency jar file
  // def depPath (name :String) = managedDependencyRootPath ** (name+"*")

  // // generates FooServiceAsync classes from FooService classes for GWT RPC
  // lazy val genasync = runTask(Some("com.samskivert.asyncgen.AsyncGenTool"),
  //                             compileClasspath +++ depPath("gwt-asyncgen"),
  //                             (mainJavaSourcePath ** "*Service.java" getPaths).toList)

  // // generates FooMessages.java from FooMessages.properties for GWT i18n
  // lazy val i18nsync = runTask(Some("com.threerings.gwt.tools.I18nSync"), compileClasspath,
  //                             mainJavaSourcePath.absolutePath :: (
  //                               mainJavaSourcePath ** "*Messages.properties" getPaths).toList)

  // // compiles our GWT client
  // def gwtc (module :String) = runTask(
  //   Some("com.google.gwt.dev.Compiler"),
  //   compileClasspath +++ depPath("gwt-dev") +++ mainJavaSourcePath +++ mainResourcesPath,
  //   List("-war", "target/scala_2.8.0/webapp", module))
  // lazy val memoryc = gwtc("memory") dependsOn(copyResources)
  // lazy val accountc = gwtc("account") dependsOn(copyResources)

  // // copy our compiled classes to WEB-INF/classes after compiling
  // override def compileAction = task {
  //   mainCompileConditional.run
  //   FileUtilities.copy(mainClasses.get, jettyWebappPath / "WEB-INF" / "classes", log).left.toOption
  // } dependsOn(i18nsync) // regenerate our i18n classes every time we compile

  // // prepares our webapp for shipping
  // lazy val prepShip = prepareWebappAction && memoryc && accountc
}
