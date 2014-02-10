package io.dropwizard.scala

import io.dropwizard.setup.{Bootstrap, Environment}
import io.dropwizard.scala.jersey.inject.CollectionsQueryParamInjectableProvider
import io.dropwizard.scala.jersey.dispatch.OptionResourceMethodDispatchAdapter
import io.dropwizard.Bundle

import com.fasterxml.jackson.module.scala.DefaultScalaModule

/** Provides Scala support to core Dropwizard functionality. */
class ScalaBundle extends Bundle {

  def initialize(bootstrap: Bootstrap[_]) {
    bootstrap.getObjectMapper.registerModule(new DefaultScalaModule)
  }

  def run(environment: Environment) {
    val jersey = environment.jersey()
    jersey.register(new CollectionsQueryParamInjectableProvider)
    jersey.register(new OptionResourceMethodDispatchAdapter)
  }
}
