// basic facts
name := "Dropwizard Scala"

normalizedName := "dropwizard-scala"

description := "Scala language integration for the Dropwizard project."

homepage in ThisBuild := Some(url("http://scala.dropwizard.io/"))

startYear in ThisBuild := Some(2014)

licenses in ThisBuild += ("Apache License 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

organization in ThisBuild := "io.dropwizard.scala"

scmInfo in ThisBuild := Some(ScmInfo(
    browseUrl = url("http://github.com/dropwizard/dropwizard-scala/"), 
    connection = "git://github.com/dropwizard/dropwizard-scala.git",
    devConnection = Some("git@github.com:dropwizard/dropwizard-scala.git")
))


// compile with Scala 2.10+
scalaVersion in ThisBuild := "2.10.3"

crossScalaVersions in ThisBuild := Seq("2.10.3")

// compile more strictly
scalacOptions in ThisBuild ++= Seq("-deprecation", "-unchecked", "-feature")

// ensure JDK 1.7+
javacOptions in ThisBuild ++= Seq("-source", "1.7", "-target", "1.7")

scalacOptions in ThisBuild += "-target:jvm-1.7"

// use local Maven repo and Sonatype snapshots for resolving dependencies
resolvers in ThisBuild ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

// dependencies
libraryDependencies in ThisBuild ++= Seq(
    "org.scalatest" %% "scalatest" % "2.1.0" % "test",
    "org.mockito" % "mockito-core" % "1.9.5" % "test"
)

// aggregate sub-modules
lazy val parent = project.in(file(".")).aggregate(core, jersey, jdbi, validation)

lazy val core = project.dependsOn(jersey).dependsOn(validation)

lazy val jersey = project

lazy val validation = project

lazy val jdbi = project

