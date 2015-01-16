name := "Dropwizard Scala (JDBI)"

normalizedName := "dropwizard-scala-jdbi"

description := "Scala language integration for Dropwizard's JDBI support."

// dependencies
libraryDependencies ++= Seq(
    "io.dropwizard" % "dropwizard-jdbi" % Versions.dropwizard
)

unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile, scalaBinaryVersion) {
  case (s, v) if v.startsWith("2.9.") => s / "scala_2.9"
  case (s, v) => s / ("scala_" + v)
}

publishTo <<= version(repository)

