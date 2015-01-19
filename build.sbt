val ScalaVersion = "(\\d+)\\.(\\d+).*".r

// basic facts
name := "Dropwizard Scala"

normalizedName := "dropwizard-scala"

description := "Scala language integration for the Dropwizard project."

homepage in ThisBuild := Some(url("http://scala.dropwizard.io/"))

startYear in ThisBuild := Some(2014)

licenses in ThisBuild += ("Apache License 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

organization in ThisBuild := "com.datasift.dropwizard.scala"

scmInfo in ThisBuild := Some(ScmInfo(
    browseUrl = url("http://github.com/dropwizard/dropwizard-scala/"), 
    connection = "git://github.com/dropwizard/dropwizard-scala.git",
    devConnection = Some("git@github.com:dropwizard/dropwizard-scala.git")
))


crossScalaVersions in ThisBuild := Seq("2.9.1", "2.9.3", "2.10.4")

// compile more strictly
scalacOptions <<= scalaVersion map { v =>
  val options = "-deprecation" :: "-unchecked" :: Nil
  if (v.startsWith("2.9.")) options else "-target:jvm-1.7" :: "-language:higherKinds" :: "-feature" :: options
}

// ensure JDK 1.7+
javacOptions in ThisBuild ++= Seq("-source", "1.7", "-target", "1.7")

// use local Maven repo and Sonatype snapshots for resolving dependencies
resolvers in ThisBuild ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

// dependencies
libraryDependencies in ThisBuild <++= scalaBinaryVersion { scalaVersion =>
  val scalaTest = scalaVersion match {
    case "2.9.3" | "2.9.2" | "2.9.1" | "2.9.0" => "2.0.M5b"
    case "2.10" | "2.11"                       => "2.1.0"
  }
  Seq(
    "org.scalatest" %% "scalatest" % scalaTest % "test",
    "org.mockito" % "mockito-core" % "1.9.5" % "test"
  ) ++ (scalaVersion match {
    // note: scala-logging is only available for Scala 2.10+
    case "2.10" | "2.11" => Seq("com.typesafe.scala-logging" %% "scala-logging-slf4j" % "2.1.2")
    case _ => Seq.empty
  })
}

// aggregate sub-modules
lazy val parent = project.in(file("."))
    .settings(publishArtifact := false)
    .aggregate(core, jersey, jdbi, validation, metrics)

lazy val core: Project = project.dependsOn(jersey).dependsOn(validation)

lazy val jersey = project

lazy val validation = project

lazy val jdbi = project

lazy val metrics = project
