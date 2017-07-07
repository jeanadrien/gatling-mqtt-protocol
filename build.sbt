name := "gatling-mqtt-protocol"

scalaVersion := "2.11.8"

version := "1.1-SNAPSHOT"

libraryDependencies += "io.gatling" % "gatling-core" % "2.2.5" % "provided"
libraryDependencies += "org.fusesource.mqtt-client" % "mqtt-client" % "1.14"

// for the gatling lib
assemblyOption in assembly := (assemblyOption in assembly).value
    .copy(includeScala = false)
