name := "Dropwizard Scala (Core)"

normalizedName := "dropwizard-scala-core"

description := "Scala language integration for Dropwizard core"

// dependencies
libraryDependencies ++= Seq(
    "io.dropwizard" % "dropwizard-core" % Versions.dropwizard,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jackson,
    "io.dropwizard" % "dropwizard-client" % Versions.dropwizard % "test"
)

unmanagedSourceDirectories in Compile <+= (sourceDirectory in Compile, scalaBinaryVersion) {
  case (s, v) if v.startsWith("2.9.") => s / "scala_2.9"
  case (s, v) => s / ("scala_" + v)
}
