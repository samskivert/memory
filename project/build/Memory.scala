import sbt._

class Memory (info :ProjectInfo) extends DefaultWebProject(info) {
  // need our local repository for gwt-utils snapshot
  val mavenLocal = "Local Maven Repository" at "file://"+Path.userHome+"/.m2/repository"
  val objectifySvn = "Objectify repo" at "http://objectify-appengine.googlecode.com/svn/maven"

  // general depends
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"
  val scalaj_collection = "org.scalaj" %% "scalaj-collection" % "1.0"

  // HTTP and GWT depends
  val gwtVers = "2.2.0"
  val gwtUser = "com.google.gwt" % "gwt-user" % gwtVers
  val gwtUtils = "com.threerings" % "gwt-utils" % "1.3-SNAPSHOT"

  // override def libraryDependencies = Set(
  //   "org.eclipse.jetty" % "jetty-server" % "7.0.0.v20091005" % "test",
  //   "org.eclipse.jetty" % "jetty-webapp" % "7.0.0.v20091005" % "test"
  // ) ++ super.libraryDependencies

  // we don't want these on any of our classpaths, so we make them "system" deps
  val gwtDev = "com.google.gwt" % "gwt-dev" % gwtVers % "system"
  val gwtServlet = "com.google.gwt" % "gwt-servlet" % gwtVers % "system"
  val gwtAsyncGen = "com.samskivert" % "gwt-asyncgen" % "1.0" % "system"

  // appengine depends
  val gaeVers = "1.4.2"
  val gae = "com.google.appengine" % "appengine-api-1.0-sdk" % gaeVers
  // val gaeTools = "com.google.appengine" % "appengine-tools-sdk" % gaeVers % "test"
  val gaeTesting = "com.google.appengine" % "appengine-testing" % gaeVers % "test"
  val gaeStubs = "com.google.appengine" % "appengine-api-stubs" % gaeVers % "test"
  val gaeLabs = "com.google.appengine" % "appengine-api-labs" % gaeVers % "test"

  // database depends
  val objectify = "com.googlecode.objectify" % "objectify" % "2.2.1"
  val jpa = "javax.persistence" % "persistence-api" % "1.0"

  // pass some useful arguments to javac
  override def javaCompileOptions = List(
    JavaCompileOption("-Xlint:all"), JavaCompileOption("-Xlint:-serial")
  ) ++ super.javaCompileOptions

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
  def gwtc (module :String) = runTask(
    Some("com.google.gwt.dev.Compiler"),
    compileClasspath +++ depPath("gwt-dev") +++ mainJavaSourcePath +++ mainResourcesPath,
    List("-war", "target/scala_2.8.0/webapp", module))
  lazy val memoryc = gwtc("memory") dependsOn(copyResources)
  lazy val accountc = gwtc("account") dependsOn(copyResources)

  // copy our compiled classes to WEB-INF/classes after compiling
  override def compileAction = task {
    mainCompileConditional.run
    FileUtilities.copy(mainClasses.get, jettyWebappPath / "WEB-INF" / "classes", log).left.toOption
  } dependsOn(i18nsync) // regenerate our i18n classes every time we compile

  // prepares our webapp for shipping
  lazy val prepShip = prepareWebappAction && memoryc && accountc
}
