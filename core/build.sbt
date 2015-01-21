libraryDependencies ++= Seq(
    "io.dropwizard" % "dropwizard-core" % Versions.dropwizard,
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jackson,
    "io.dropwizard" % "dropwizard-client" % Versions.dropwizard % "test"
)

