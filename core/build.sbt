libraryDependencies ++= Seq(
  "io.dropwizard" % "dropwizard-core" % Versions.dropwizard,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % Versions.jackson,

  "io.dropwizard" % "dropwizard-client" % Versions.dropwizard % Test,
  "io.dropwizard" % "dropwizard-migrations" % Versions.dropwizard % Test,
  "mysql" % "mysql-connector-mxj" % "5.0.12" % Test
)
