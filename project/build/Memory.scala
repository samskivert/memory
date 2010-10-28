import sbt._

class Memory (info :ProjectInfo) extends DefaultWebProject(info) {
  // need our local repository for gwt-utils snapshot
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"

  // general depends
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
  val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.0"

  // HTTP and GWT depends
  val gwtUser = "com.google.gwt" % "gwt-user" % "2.0.4"
  val gwtUtils = "com.threerings" % "gwt-utils" % "1.2-SNAPSHOT"

  // override def libraryDependencies = Set(
  //   "org.eclipse.jetty" % "jetty-server" % "7.0.0.v20091005" % "test",
  //   "org.eclipse.jetty" % "jetty-webapp" % "7.0.0.v20091005" % "test"
  // ) ++ super.libraryDependencies

  // we don't want these on any of our classpaths, so we make them "system" deps
  val gwtDev = "com.google.gwt" % "gwt-dev" % "2.0.4" % "system"
  val gwtServlet = "com.google.gwt" % "gwt-servlet" % "2.0.4" % "system"
  val gwtAsyncGen = "com.samskivert" % "gwt-asyncgen" % "1.0" % "system"

  // database depends
  val h2db = "com.h2database" % "h2" % "1.2.142"
  val squeryl = "org.squeryl" % "squeryl_2.8.0" % "0.9.4-RC2"

  // used to obtain the path for a specific dependency jar file
  def depPath (name :String) = managedDependencyRootPath ** (name+"*")

  // generates FooServiceAsync classes from FooService classes for GWT RPC
  lazy val genasync = runTask(Some("com.samskivert.asyncgen.AsyncGenTool"),
                              compileClasspath +++ depPath("gwt-asyncgen"),
                              (mainJavaSourcePath ** "*Service.java" getPaths).toList)

  // generates FooMessages.java from FooMessages.properties for GWT i18n
  lazy val i18nsync = runTask(Some("com.threerings.gwt.tools.I18nSync"), compileClasspath,
                              mainJavaSourcePath.absolutePath :: (
                                mainJavaSourcePath ** "*Messages.properties" getPaths).toList)

  // compiles our GWT client
  lazy val gwtc = runTask(
    Some("com.google.gwt.dev.Compiler"),
    compileClasspath +++ depPath("gwt-dev") +++ mainJavaSourcePath +++ mainResourcesPath,
    List("-war", "target/scala_2.8.0/webapp", "memory")) dependsOn(copyResources)

  // regenerate our i18n classes every time we compile
  override def compileAction = super.compileAction dependsOn(i18nsync)
}
