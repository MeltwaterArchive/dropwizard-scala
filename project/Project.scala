import sbt._
import Keys._

object Versions {
  val dropwizard = "0.7.1"
  val jackson = "2.3.3"
}

object DropwizardScala extends Build {

    def repository(version: String) = {
      val nexus = "https://oss.sonatype.org/"
      if (version.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases" at nexus + "service/local/staging/deploy/maven2")
    }
}
