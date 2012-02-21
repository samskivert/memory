resolvers += "GWT plugin repo" at "http://thunderklaus.github.com/maven"

addSbtPlugin("net.thunderklaus" % "sbt-gwt-plugin" % "1.1-SNAPSHOT")

libraryDependencies ++= Seq(
  "com.samskivert" % "gwt-asyncgen" % "1.0",
  "com.threerings" % "gwt-utils" % "1.5"
)
