package net.nicktelford.dropwizard.scala

import io.dropwizard.setup.{Bootstrap, Environment}
import net.nicktelford.dropwizard.scala.jersey.inject.CollectionsQueryParamInjectableProvider
import net.nicktelford.dropwizard.scala.jersey.dispatch.OptionResourceMethodDispatchAdapter
import io.dropwizard.Bundle

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import net.nicktelford.dropwizard.scala.validation.ScalaValidatorsBundle

/** Provides Scala support to core Dropwizard functionality. */
class ScalaBundle extends Bundle {

  val validatorsBundle = new ScalaValidatorsBundle

  def initialize(bootstrap: Bootstrap[_]) {
    bootstrap.getObjectMapper.registerModule(new DefaultScalaModule)
    validatorsBundle.initialize(bootstrap)
  }

  def run(environment: Environment) {
    val jersey = environment.jersey()
    jersey.register(new CollectionsQueryParamInjectableProvider)
    jersey.register(new OptionResourceMethodDispatchAdapter)
    validatorsBundle.run(environment)
  }
}
