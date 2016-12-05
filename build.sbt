name := "gatling-mqtt-protocol"

scalaVersion := "2.11.8"

version := "1.0-SNAPSHOT"

libraryDependencies += "io.gatling" % "gatling-core" % "2.2.3" % "provided"
libraryDependencies += "org.fusesource.mqtt-client" % "mqtt-client" % "1.14"

// No : Move to test
libraryDependencies  += "com.typesafe.play" %% "play-json" % "2.5.10"

// No : remove
libraryDependencies += "io.gatling" % "gatling-http" % "2.2.3" % "provided"

// for the gatling lib
assemblyOption in assembly := (assemblyOption in assembly).value
    .copy(includeScala = false)
