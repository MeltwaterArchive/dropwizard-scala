libraryDependencies ++= Seq(
  "io.dropwizard" % "dropwizard-core" % Versions.dropwizard,
  "io.dropwizard" % "dropwizard-client" % Versions.dropwizard,
  "org.scalatest" %% "scalatest" % "3.0.1",
  "io.dropwizard" % "dropwizard-migrations" % Versions.dropwizard % "optional",
  "mysql" % "mysql-connector-mxj" % "5.0.12" % "optional"
)

