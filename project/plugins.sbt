// make releases
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.10")

// sign releases
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.1.2")

// release to Sonatype OSS
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "2.3")
