import PgpKeys._
import sbtrelease._
import ReleaseKeys._
import ReleaseStateTransformations._

// publishing
publishMavenStyle := true

publishTo <<= version(repository)

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

