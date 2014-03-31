// make releases
addSbtPlugin("com.github.gseitz" % "sbt-release" % "0.8")

// sign releases
addSbtPlugin("com.typesafe.sbt" % "sbt-pgp" % "0.8.1")

// generate IDEA project
addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

// release to Sonatype OSS
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "0.2.1")

