import com.typesafe.sbt.pgp.PgpKeys
import Keys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease._

def repository(isSnapshot: Boolean) = {
    val nexus = "https://oss.sonatype.org/"
        if (isSnapshot)
            Some("snapshots" at nexus + "content/repositories/snapshots")
        else
            Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

lazy val commonSettings = Seq(

  description := "Scala language integration for the Dropwizard project.",
  homepage := Option(url("http://github.com/datasift/dropwizard-scala")),
  startYear := Option(2014),
  licenses += ("Apache License 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html")),
  organization := "com.datasift.dropwizard.scala",
  scmInfo := Option(ScmInfo(
    browseUrl = url("http://github.com/dropwizard/dropwizard-scala/"),
    connection = "git://github.com/dropwizard/dropwizard-scala.git",
    devConnection = Option("git@github.com@:dropwizard/dropwizard-scala.git")
  )),
  scalaVersion := "2.13.4",
  crossScalaVersions := Seq("2.11.12", "2.12.12", "2.13.4"),
  scalacOptions ++=
    "-deprecation" ::
      "-unchecked" ::
      "-language:implicitConversions" ::
      "-language:higherKinds" ::
      "-feature" ::
      Nil,

  javacOptions ++= "-source" :: "1.8" :: "-target" :: "1.8" :: Nil,
  resolvers in ThisBuild ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
  ),
  libraryDependencies ++= Seq(
    "org.log4s" %% "log4s" % "1.9.0",
    "org.scalatest" %% "scalatest" % Versions.scalaTest % "test",
    "org.mockito"   %% "mockito-scala" % Versions.mockitoScala % "test"
  ),
  publishMavenStyle := true,
  publishTo := isSnapshot(repository).value,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  pomExtra := {
    <developers>
      <developer>
        <id>nicktelford</id>
        <name>Nick Telford</name>
        <email>nick.telford@gmail.com</email>
      </developer>
    </developers>
  },
  unmanagedSourceDirectories in Compile +=
    (sourceDirectory in Compile).value / ("scala_" + scalaBinaryVersion.value),
  PgpKeys.useGpg := true,
  PgpKeys.useGpgAgent := true,
  releaseCrossBuild := true,
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseVersion := identity[String],
  releaseNextVersion := { Version(_).map { v =>
    v.withoutQualifier.string + "-" + v.qualifier
      .flatMap(x => scala.util.Try(x.stripPrefix("-").toInt).toOption)
      .map(_ + 1).getOrElse(1)
  }.getOrElse(versionFormatError(version.value)) }
)

lazy val core = (project in file("core"))
  .settings(
    name := "Dropwizard scala core",
    normalizedName := "dropwizard-scala-core",
    commonSettings
  )
  .dependsOn(jersey, validation, metrics, test % "test", jdbi % "test")

lazy val jersey = (project in file("jersey"))
  .settings(
    name := "Dropwizard scala jersey",
    normalizedName := "dropwizard-scala-jersey",
    commonSettings
  )

lazy val validation = (project in file("validation"))
  .settings(
    name := "Dropwizard scala validation",
    normalizedName := "dropwizard-scala-validation",
    commonSettings
  )

lazy val jdbi = (project in file("jdbi"))
  .settings(
    name := "Dropwizard scala jdbi",
    normalizedName := "dropwizard-scala-jdbi",
    commonSettings
  )

lazy val metrics = (project in file("metrics"))
  .settings(
    name := "Dropwizard scala metrics",
    normalizedName := "dropwizard-scala-metrics",
    commonSettings
  )

lazy val test = (project in file("test"))
  .settings(
    name := "Dropwizard scala test",
    normalizedName := "dropwizard-scala-test",
    commonSettings
  )

lazy val parent = (project in file("."))
  .settings(
    commonSettings,
    name := "Dropwizard scala parent",
    normalizedName := "dropwizard-scala-parent",
    publishArtifact := false,
    Keys.`package` := file(""),
    packageBin in Global := file(""),
    packagedArtifacts := Map()
  )
  .aggregate(core, jersey, jdbi, validation, metrics, test)

