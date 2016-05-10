package com.datasift.dropwizard.scala.jersey.inject

import javax.ws.rs.{HeaderParam, FormParam, QueryParam}

import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver

object ScalaParamInjectionResolver {

  class QueryParamInjectionResolver
    extends ParamInjectionResolver[QueryParam](
      classOf[CollectionQueryParamFactoryProvider])

  class FormParamInjectionResolver
    extends ParamInjectionResolver[FormParam](
      classOf[CollectionFormParamFactoryProvider])

  class HeaderParamInjectionResolver
    extends ParamInjectionResolver[HeaderParam](
      classOf[CollectionHeaderParamFactoryProvider])

}
