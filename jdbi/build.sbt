libraryDependencies ++= Seq(
    "io.dropwizard" % "dropwizard-jdbi" % Versions.dropwizard,
    "io.dropwizard" % "dropwizard-client" % Versions.dropwizard % "test",
    "com.h2database" % "h2" % "1.4.189" % "test"
)

