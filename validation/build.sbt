name := "Dropwizard Scala (Validation)"

normalizedName := "dropwizard-scala-validation"

description := "Scala language integration for Dropwizard's validation."

// dependencies
libraryDependencies ++= Seq(
    "io.dropwizard" % "dropwizard-core" % Versions.dropwizard,
    "io.dropwizard" % "dropwizard-validation" % Versions.dropwizard
)

