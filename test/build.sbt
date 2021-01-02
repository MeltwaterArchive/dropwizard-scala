libraryDependencies ++= Seq(
  "io.dropwizard" % "dropwizard-core" % Versions.dropwizard,
  "io.dropwizard" % "dropwizard-client" % Versions.dropwizard,
  "org.scalatest" %% "scalatest" % "3.2.3",
  "org.mockito"   %% "mockito-scala" % Versions.mockitoScala,
  "io.dropwizard" % "dropwizard-migrations" % Versions.dropwizard % "optional",
  "mysql" % "mysql-connector-mxj" % "5.0.12" % "optional"
)

