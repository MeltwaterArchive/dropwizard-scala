name := "Dropwizard Scala (JDBI)"

normalizedName := "dropwizard-scala-jdbi"

description := "Scala language integration for Dropwizard's JDBI support."

// dependencies
libraryDependencies ++= Seq(
    "io.dropwizard" % "dropwizard-jdbi" % Versions.dropwizard
)

