name := "Dropwizard Scala (Jersey)"

normalizedName := "dropwizard-scala-jersey"

description := "Scala language integration for Dropwizard's Jersey support."

// dependencies
libraryDependencies ++= Seq(
    "io.dropwizard" % "dropwizard-jersey" % Versions.dropwizard
)

