import com.typesafe.sbt.pgp.PgpKeys
import sbt._
import Keys._
import sbtrelease.ReleasePlugin.autoImport._
import sbtrelease._

object Versions {

  val dropwizard = "1.0.0"
  val jackson = "2.7.5" // DW 1.0.0 uses jackson 2.7.6, but jackson-module-scala 2.7.5 is latest
  val mockito = "1.10.19"
  val scalaTest = "2.2.6"
}

object CompileOptions {

  def scala(scalaVersion: String): Seq[String] =
    "-deprecation" ::
      "-unchecked" ::
      "-language:implicitConversions" ::
      "-language:higherKinds" ::
      "-feature" ::
      (scalaVersion match {
        case v if v.startsWith("2.11.") && v.stripPrefix("2.11.").toInt > 4 =>
          "-target:jvm-1.8"
        case _ =>
          "-target:jvm-1.7"
      }) ::
      Nil

  val java: Seq[String] = Seq("-source", "1.8", "-target", "1.8")
}

object DropwizardScala extends Build {

  val buildSettings = super.settings ++ Seq(
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
    crossScalaVersions := Seq("2.10.6", "2.11.8"),
    scalacOptions <++= scalaVersion.map(CompileOptions.scala),
    javacOptions ++= CompileOptions.java,
    resolvers in ThisBuild ++= Seq(
      "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    ),
    libraryDependencies ++= Seq(
      "org.log4s" %% "log4s" % "1.3.0",
      "org.scalatest" %% "scalatest" % Versions.scalaTest % "test",
      "org.mockito" % "mockito-core" % Versions.mockito % "test"
    ),
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
      case (s, v) => s / ("scala_" + v) :: Nil
    },
    PgpKeys.useGpg := true,
    PgpKeys.useGpgAgent := true,
    releaseCrossBuild := true,
    releasePublishArtifactsAction := PgpKeys.publishSigned.value,
    releaseVersion := identity[String],
    releaseNextVersion := { Version(_).map { v =>
      v.withoutQualifier.string + "-" + v.qualifier
        .flatMap(x => util.Try(x.stripPrefix("-").toInt).toOption)
        .map(_ + 1).getOrElse(1)
    }.getOrElse(versionFormatError) }
  )

  def module(id: String): sbt.Project = module(id, file(id), Nil)
  def module(id: String, file: sbt.File, settings: Seq[Def.Setting[_]]): sbt.Project = {
    Project(id, file, settings = buildSettings ++ settings ++ Seq(
      name := "Dropwizard Scala %s".format(id),
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

  lazy val core = module("core")
    .dependsOn(jersey, validation, metrics, test % "test", jdbi % "test")
  lazy val jersey = module("jersey")
  lazy val validation = module("validation")
  lazy val jdbi = module("jdbi")
  lazy val metrics = module("metrics")
  lazy val test = module("test")

  lazy val parent = module("parent", file("."), Seq(
      publishArtifact := false,
      Keys.`package` := file(""),
      packageBin in Global := file(""),
      packagedArtifacts := Map()))
    .aggregate(core, jersey, jdbi, validation, metrics, test)
}
