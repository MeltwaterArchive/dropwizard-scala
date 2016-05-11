package com.datasift.dropwizard.scala.jersey.inject

import java.io.OutputStream
import java.lang.annotation.Annotation
import java.lang.reflect.Type
import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.{MultivaluedMap, MediaType}

import com.datasift.dropwizard.scala.jersey.ParameterizedMessageBodyWriter

class EitherMessageBodyWriter
  extends ParameterizedMessageBodyWriter[Either[_, _]] {

  override def writeTo(option: Either[_, _],
                       rawClass: Class[_],
                       genericType: Type,
                       annotations: Array[Annotation],
                       mediaType: MediaType,
                       httpHeaders: MultivaluedMap[String, AnyRef],
                       entityStream: OutputStream): Unit = option match {
    case Left(left)
      if classOf[WebApplicationException].isAssignableFrom(left.getClass) =>
      throw left.asInstanceOf[WebApplicationException]
    case Left(left) if classOf[Throwable].isAssignableFrom(left.getClass) =>
      throw left.asInstanceOf[Throwable]
    case Left(left) =>
      val klass = left.getClass
      getTypeArgument(genericType, 0).foreach { tpe =>
        getWriter(klass, tpe, annotations, mediaType).foreach {
          _.writeTo(
            left.asInstanceOf[Any],
            klass,
            tpe,
            annotations,
            mediaType,
            httpHeaders,
            entityStream
          )
        }
      }
    case Right(right) =>
      val klass = right.getClass
      getTypeArgument(genericType, 1).foreach { tpe =>
        getWriter(klass, tpe, annotations, mediaType).foreach { writer =>
          writer.writeTo(
            right.asInstanceOf[Any],
            klass,
            tpe,
            annotations,
            mediaType,
            httpHeaders,
            entityStream)
        }
      }
  }
}
