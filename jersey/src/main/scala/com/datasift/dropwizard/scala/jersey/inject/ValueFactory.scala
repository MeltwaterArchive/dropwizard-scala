package com.datasift.dropwizard.scala.jersey.inject

import javax.ws.rs.ProcessingException
import javax.ws.rs.core.Form

import org.glassfish.jersey.internal.inject.ExtractorException
import org.glassfish.jersey.server.ParamException._
import org.glassfish.jersey.server.internal.inject.{MultivaluedParameterExtractor, AbstractContainerRequestValueFactory}

class QueryParamValueFactory(extractor: MultivaluedParameterExtractor[_],
                             decode: Boolean)
  extends AbstractContainerRequestValueFactory[AnyRef] {

  override def provide(): AnyRef = try {
    val parameters = getContainerRequest
      .getUriInfo
      .getQueryParameters(decode)

    extractor.extract(parameters).asInstanceOf[AnyRef]
  } catch {
    case e: ExtractorException => throw new QueryParamException(
        e.getCause, extractor.getName, extractor.getDefaultValueString)
  }
}

class FormParamValueFactory(extractor: MultivaluedParameterExtractor[_],
                            decode: Boolean)
  extends AbstractContainerRequestValueFactory[AnyRef] {

  override def provide(): AnyRef = try {
    getContainerRequest.bufferEntity()
    val form = getContainerRequest.readEntity(classOf[Form])
    extractor.extract(form.asMap()).asInstanceOf[AnyRef]
  } catch {
    case e: ProcessingException => throw new FormParamException(
      e.getCause, extractor.getName, extractor.getDefaultValueString)
  }
}

class HeaderParamValueFactory(extractor: MultivaluedParameterExtractor[_],
                              decode: Boolean)
  extends AbstractContainerRequestValueFactory[AnyRef] {

  override def provide(): AnyRef = try {
    extractor.extract(getContainerRequest.getHeaders).asInstanceOf[AnyRef]
  } catch {
    case e: ExtractorException => throw new HeaderParamException(
      e.getCause, extractor.getName, extractor.getDefaultValueString)
  }
}

