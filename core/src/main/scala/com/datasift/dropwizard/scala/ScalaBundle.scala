package com.datasift.dropwizard.scala

import com.datasift.dropwizard.scala.jersey.inject.{ScalaInjectionBinder, OptionMessageBodyWriter}
import io.dropwizard.setup.{Bootstrap, Environment}
import io.dropwizard.Bundle

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.datasift.dropwizard.scala.validation.ScalaValidatorsBundle

/** Provides Scala support to core Dropwizard functionality. */
class ScalaBundle extends Bundle {

  val validatorsBundle = new ScalaValidatorsBundle

  override def initialize(bootstrap: Bootstrap[_]) {
    bootstrap.getObjectMapper.registerModule(new DefaultScalaModule)
    validatorsBundle.initialize(bootstrap)
  }

  override def run(environment: Environment) {
    val jersey = environment.jersey()
    jersey.register(new OptionMessageBodyWriter)
    jersey.register(new ScalaInjectionBinder)
    validatorsBundle.run(environment)
  }
}
