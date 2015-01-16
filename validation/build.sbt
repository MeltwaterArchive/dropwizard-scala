name := "Dropwizard Scala (Validation)"

normalizedName := "dropwizard-scala-validation"

description := "Scala language integration for Dropwizard's validation."

// dependencies
libraryDependencies ++= Seq(
    "io.dropwizard" % "dropwizard-core" % Versions.dropwizard,
    "io.dropwizard" % "dropwizard-validation" % Versions.dropwizard
)

// add extra source directories
unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile, scalaBinaryVersion) {
  case (s, v) if v.startsWith("2.9.") => s / "scala_2.9"
  case (s, v) => s / ("scala_" + v)
}

publishTo <<= version(repository)

