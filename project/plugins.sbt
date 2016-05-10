// make releases
addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")

// sign releases
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

// generate IDEA project
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// release to Sonatype OSS
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "1.1")
