// globally defines the version of Dropwizard to build against
lazy val dropwizardVersion = settingKey[String]("Dropwizard version")

dropwizardVersion := "0.7.0-rc1"

// basic facts
name := "Dropwizard Scala"

normalizedName := "dropwizard-scala"

description := "Scala language integration for the Dropwizard project."

homepage := Some(url("http://scala.dropwizard.io/"))

startYear := Some(2014)

licenses += ("Apache License 2.0", url("http://www.apache.org/licenses/LICENSE-2.0.html"))

organization := "io.dropwizard"

scmInfo := Some(ScmInfo(
    browseUrl = url("http://github.com/dropwizard/dropwizard-scala/"), 
    connection = "git://github.com/dropwizard/dropwizard-scala.git",
    devConnection = Some("git@github.com:dropwizard/dropwizard-scala.git")
))


// compile with Scala 2.10+
scalaVersion := "2.10.3"

crossScalaVersions := Seq("2.10.3")

// compile more strictly
scalacOptions ++= Seq("-deprecation", "-unchecked")

// ensure JDK 1.7+
javacOptions ++= Seq("-source", "1.7", "-target", "1.7")

scalacOptions += "-target:jvm-1.7"

// use local Maven repo and Sonatype snapshots for resolving dependencies
resolvers ++= Seq(
    "Local Maven Repository" at "file://" + Path.userHome.absolutePath + "/.m2/repository",
    "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

// dependencies
libraryDependencies ++= Seq(
    "io.dropwizard" % "dropwizard-core" % dropwizardVersion.value,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.3.1"
)

