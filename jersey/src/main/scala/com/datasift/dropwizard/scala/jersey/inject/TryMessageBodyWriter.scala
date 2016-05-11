package com.datasift.dropwizard.scala.jersey.inject

import java.io.OutputStream
import java.lang.annotation.Annotation
import java.lang.reflect.Type
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.{MultivaluedMap, MediaType}

import com.datasift.dropwizard.scala.jersey.ParameterizedMessageBodyWriter

import scala.util.{Success, Failure, Try}

class TryMessageBodyWriter extends ParameterizedMessageBodyWriter[Try[_]] {

  override def writeTo(value: Try[_],
                       rawClass: Class[_],
                       genericType: Type,
                       annotations: Array[Annotation],
                       mediaType: MediaType,
                       httpHeaders: MultivaluedMap[String, AnyRef],
                       entityStream: OutputStream): Unit = value match {
    case Failure(t) if t.isInstanceOf[WebApplicationException] => throw t
    case Failure(t) => throw new WebApplicationException(t)
    case Success(data) =>
      val klass = data.getClass
      getTypeArgument(genericType, 0).foreach { innerGenericType =>
        getWriter(klass, innerGenericType, annotations, mediaType).foreach {
          _.writeTo(
            data,
            klass,
            innerGenericType,
            annotations,
            mediaType,
            httpHeaders,
            entityStream)
        }
      }
  }
}
