package com.datasift.dropwizard.scala.jersey.inject

import javax.inject.Singleton
import javax.ws.rs.ext.ParamConverterProvider
import javax.ws.rs.{FormParam, HeaderParam, QueryParam}

import org.glassfish.hk2.api.{InjectionResolver, TypeLiteral}
import org.glassfish.hk2.utilities.binding.AbstractBinder
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider

import ScalaParamInjectionResolver._

class ScalaInjectionBinder extends AbstractBinder {
  override def configure(): Unit = {
    bind(classOf[CollectionQueryParamFactoryProvider])
      .to(classOf[ValueFactoryProvider])
      .in(classOf[Singleton])
    bind(classOf[CollectionFormParamFactoryProvider])
      .to(classOf[ValueFactoryProvider])
      .in(classOf[Singleton])
    bind(classOf[CollectionHeaderParamFactoryProvider])
      .to(classOf[ValueFactoryProvider])
      .in(classOf[Singleton])

    bind(classOf[QueryParamInjectionResolver])
      .to(new TypeLiteral[InjectionResolver[QueryParam]] {})
      .in(classOf[Singleton])

    bind(classOf[FormParamInjectionResolver])
      .to(new TypeLiteral[InjectionResolver[FormParam]] {})
      .in(classOf[Singleton])

    bind(classOf[HeaderParamInjectionResolver])
      .to(new TypeLiteral[InjectionResolver[HeaderParam]] {})
      .in(classOf[Singleton])

    bind(classOf[ScalaParamConvertersProvider])
      .to(classOf[ParamConverterProvider])
      .in(classOf[Singleton])
  }
}
