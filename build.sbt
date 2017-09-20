
name := "gatling-mqtt-protocol"

organization := "com.github.jeanadrien"

scalaVersion := "2.11.8"

version := "1.1-SNAPSHOT"

homepage := Some(url("https://github.com/jeanadrien/evrythng-scala-sdk"))

scmInfo := Some(
    ScmInfo(
        url("https://github.com/jeanadrien/gatling-mqtt-protocol"),
        "scm:git@github.com:jeanadrien/gatling-mqtt-protocol.git"
    )
)

developers := List(
    Developer(
        id    = "jeanadrien",
        name  = "Jean-Adrien Vaucher",
        email = "jean@jeanjean.ch",
        url   = url("https://github.com/jeanadrien")
    )
)

libraryDependencies += "io.gatling" % "gatling-core" % "2.2.5" % "provided"
libraryDependencies += "org.fusesource.mqtt-client" % "mqtt-client" % "1.14"
libraryDependencies += "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.2.5" % "test"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % "2.2.5" % "test"

// for the gatling lib
assemblyOption in assembly := (assemblyOption in assembly).value
    .copy(includeScala = false)

enablePlugins(GatlingPlugin)

