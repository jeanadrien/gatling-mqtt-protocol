import sbt.Keys._
import sbt._
import sbtrelease.ReleasePlugin.autoImport.ReleaseTransformations._
import sbtrelease.ReleasePlugin.autoImport._

object ReleaseProcess {

    val settings = Seq(
        releaseIgnoreUntrackedFiles := true,
        releaseTagComment := s"Release ${(version in ThisBuild).value}",
        releaseCommitMessage := s"Set version to ${(version in ThisBuild).value}",
        releaseCrossBuild := false
    )

    val runPublishSigned = ReleaseStep(action = Command.process("publishSigned", _))
    val runScalastyle = ReleaseStep(action = Command.process("scalastyle", _))
    val runTestScalastyle = ReleaseStep(action = Command.process("test:scalastyle", _))

    val process =  Seq[ReleaseStep](
        checkSnapshotDependencies,
        inquireVersions,
        runTest,
        runScalastyle,
        runTestScalastyle,
        setReleaseVersion,
        commitReleaseVersion,
        tagRelease,
        runPublishSigned,
        setNextVersion,
        commitNextVersion,
        pushChanges
    )

}
