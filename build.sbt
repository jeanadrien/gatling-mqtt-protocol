organization := "com.github.jeanadrien"
name := "gatling-mqtt-protocol"

scalaVersion := "2.12.4"

licenses := Seq(
    "Apache License, Version 2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
)

homepage := Some(url("https://github.com/jeanadrien/gatling-mqtt-protocol"))

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

// publish setup
publishTo := {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot.value)
        Some("snapshots" at nexus + "content/repositories/snapshots")
    else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

// dependencies
libraryDependencies += "io.gatling" % "gatling-core" % "2.3.1" % "provided"
libraryDependencies += "org.fusesource.mqtt-client" % "mqtt-client" % "1.14"
libraryDependencies += "org.eclipse.paho" % "org.eclipse.paho.client.mqttv3" % "1.0.2"

libraryDependencies += "io.gatling.highcharts" % "gatling-charts-highcharts" % "2.3.1" % "test"
libraryDependencies += "io.gatling"            % "gatling-test-framework"    % "2.3.1" % "test"

// for the gatling lib
assemblyOption in assembly := (assemblyOption in assembly).value
    .copy(includeScala = false)

// gatling test
enablePlugins(GatlingPlugin)

// build settings
pomIncludeRepository := { _ => false }
publishMavenStyle := true
publishArtifact in Test := false

lazy val root = project.in(file("."))
    .settings(releaseProcess := ReleaseProcess.process)
