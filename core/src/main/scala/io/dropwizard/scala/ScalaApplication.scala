package io.dropwizard.scala

import io.dropwizard.setup.Bootstrap
import io.dropwizard.{Application, Configuration}

/** Base class for Dropwizard Services built in Scala. */
trait ScalaApplication[A <: Configuration] extends Application[A] {

  /** Entry point for this Dropwizard [[io.dropwizard.Application]].
    *
    * @param args the command-line arguments the program was invoked with.
    */
  final def main(args: Array[String]) {
    run(args)
  }

  /** Service initialization.
    *
    * Ensures that [[io.dropwizard.scala.ScalaBundle]] is always included in Scala
    * services.
    *
    * To customize initialization behaviour, override `ScalaService#init(Bootstrap)`.
    *
    * @param bootstrap Service Bootstrap environment.
    */
  override final def initialize(bootstrap: Bootstrap[A]) {
    bootstrap.addBundle(new ScalaBundle)
    init(bootstrap)
  }

  /** Service initialization.
    *
    * @param bootstrap Service Bootstrap environment.
    */
  def init(bootstrap: Bootstrap[A]) {
    // do nothing extra by default, override to add additional initialization behavior
  }
}
