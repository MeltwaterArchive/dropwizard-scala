import sbt._
import Keys._

import sbtrelease.ReleasePlugin._
import sbtrelease.ReleasePlugin.ReleaseKeys._

/*
import PgpKeys._
import sbtrelease._
import ReleaseKeys._
import ReleaseStateTransformations._

// use maven style tag name
tagName <<= (name, version in ThisBuild) map { (n,v) => n + "-" + v }
 */

object Versions {

  val dropwizard = "0.7.1"
  val jackson = "2.3.3"

  val mockito = "1.9.5"

  val project  = Versions.dropwizard + "-2-SNAPSHOT"
}

case class Versions(scalaBinaryVersion: String) {

  val dropwizard = Versions.dropwizard
  val jackson = Versions.jackson

  val mockito = Versions.mockito
  val scalaTest = scalaBinaryVersion match {
    case "2.10" | "2.11"           => "2.1.0"
    case v if v.startsWith("2.9.") => "2.0.M5b"
  }
}

case class Dependencies(scalaBinaryVersion: String) {

  private val versions = Versions(scalaBinaryVersion)

  val compile = scalaBinaryVersion match {
    // note: scala-logging is only available for Scala 2.10+
    case "2.10" | "2.11" => Seq("com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2")
    case _ => Seq.empty
  }

  val test = Seq(
    "org.scalatest" %% "scalatest" % versions.scalaTest % "test",
    "org.mockito" % "mockito-core" % versions.mockito % "test"
  )
}

object CompileOptions {

  def scala(scalaBinaryVersion: String): Seq[String] =
    "-deprecation" ::
      "-unchecked" ::
      (scalaBinaryVersion match {
        case v if !v.startsWith("2.9.") =>
          "-target:jvm-1.7" ::
            "-language:implicitConversions" ::
            "-language:higherKinds" ::
            "-feature" ::
            Nil
        case _ => Nil
      })

  val java: Seq[String] = Seq("-source", "1.7", "-target", "1.7")
}

object DropwizardScala extends Build {

  val buildSettings = super.settings ++ releaseSettings ++ Seq(
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
    crossScalaVersions := Seq("2.9.1", "2.9.3", "2.10.4"),
    scalacOptions <++= scalaBinaryVersion.map(CompileOptions.scala),
    javacOptions ++= CompileOptions.java,
    resolvers in ThisBuild ++= Seq(
      "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    ),
    libraryDependencies <++= scalaBinaryVersion(Dependencies).apply(_.compile),
    libraryDependencies <++= scalaBinaryVersion(Dependencies).apply(_.test),
    publishMavenStyle := true,
    publishTo <<= isSnapshot(repository),
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
    unmanagedSourceDirectories in Compile <++= (sourceDirectory in Compile, scalaBinaryVersion) {
      case (s, v) if v.startsWith("2.9.") => s / ("scala_" + v) :: s / "scala_2.9" :: Nil
      case (s, v) => s / ("scala_" + v) :: Nil
    },
    version := Versions.project,
    tagName <<= (name, version in ThisBuild) map { (n,v) => n + "-" + v }
  )

  def module(id: String): sbt.Project = module(id, file(id), Nil)
  def module(id: String, file: sbt.File, settings: Seq[Def.Setting[_]]): sbt.Project = {
    Project(id, file, settings = buildSettings ++ settings ++ Seq(
      name := "(%s)".format(id),
      normalizedName := "dropwizard-scala-%s".format(id)
    ))
  }

  def repository(isSnapshot: Boolean) = {
    val nexus = "https://oss.sonatype.org/"
    if (isSnapshot)
      Some("snapshots" at nexus + "content/repositories/snapshots")
    else
      Some("releases" at nexus + "service/local/staging/deploy/maven2")
  }

  lazy val core = module("core").dependsOn(jersey, validation)
  lazy val jersey = module("jersey")
  lazy val validation = module("validation")
  lazy val jdbi = module("jdbi")
  lazy val metrics = module("metrics")

  lazy val parent = module("parent", file("."), Seq(
      publishArtifact := false,
      Keys.`package` := file(""),
      packageBin in Global := file(""),
      packagedArtifacts := Map()))
    .aggregate(core, jersey, jdbi, validation, metrics)
}
