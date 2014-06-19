import PgpKeys._
import sbtrelease._
import ReleaseKeys._
import ReleaseStateTransformations._

// publishing
publishMavenStyle := true

publishTo <<= version { v =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := {
  <developers>
    <developer>
      <id>nicktelford</id>
      <name>Nick Telford</name>
      <email>nick.telford@gmail.com</email>
    </developer>
  </developers>
}

// release
releaseSettings

// bump bugfix on release
nextVersion := { ver => Version(ver).map(_.bumpBugfix.asSnapshot.string).getOrElse(versionFormatError) }

// use maven style tag name
tagName <<= (name, version in ThisBuild) map { (n,v) => n + "-" + v }

