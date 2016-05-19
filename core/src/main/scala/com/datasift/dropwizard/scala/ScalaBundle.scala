package com.datasift.dropwizard.scala

import com.datasift.dropwizard.scala.jersey.inject.{EitherMessageBodyWriter, TryMessageBodyWriter, ScalaInjectionBinder, OptionMessageBodyWriter}
import com.fasterxml.jackson.databind.introspect.{JacksonAnnotationIntrospector, AnnotationIntrospectorPair}
import com.fasterxml.jackson.module.scala.introspect.ScalaAnnotationIntrospector
import io.dropwizard.setup.{Bootstrap, Environment}
import io.dropwizard.Bundle

import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.datasift.dropwizard.scala.validation.ScalaValidatorsBundle

/** Provides Scala support to core Dropwizard functionality. */
class ScalaBundle extends Bundle {

  val validatorsBundle = new ScalaValidatorsBundle

  override def initialize(bootstrap: Bootstrap[_]) {
    val mapper = bootstrap.getObjectMapper
    mapper.registerModule(new DefaultScalaModule)
    mapper.setAnnotationIntrospector(new AnnotationIntrospectorPair(
      ScalaAnnotationIntrospector,
      new JacksonAnnotationIntrospector))
    validatorsBundle.initialize(bootstrap)
  }

  override def run(environment: Environment) {
    val jersey = environment.jersey()
    jersey.register(new OptionMessageBodyWriter)
    jersey.register(new TryMessageBodyWriter)
    jersey.register(new EitherMessageBodyWriter)
    jersey.register(new ScalaInjectionBinder)
    validatorsBundle.run(environment)
  }
}
