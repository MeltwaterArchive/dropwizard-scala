package com.datasift.dropwizard.scala.jersey.inject

import java.io.OutputStream
import java.lang.annotation.Annotation
import java.lang.reflect.{ParameterizedType, Type}
import javax.inject.{Provider, Inject}
import javax.ws.rs.NotFoundException
import javax.ws.rs.core.{MultivaluedMap, MediaType}
import javax.ws.rs.ext.MessageBodyWriter

import org.glassfish.jersey.message.MessageBodyWorkers

class OptionMessageBodyWriter extends MessageBodyWriter[Option[_]] {

  @Inject
  private var mbw: Provider[MessageBodyWorkers] = null

  override def writeTo(option: Option[_],
                       rawType: Class[_],
                       genericType: Type,
                       annotations: Array[Annotation],
                       mediaType: MediaType,
                       httpHeaders: MultivaluedMap[String, AnyRef],
                       entityStream: OutputStream): Unit = option match {
    case None => throw new NotFoundException
    case Some(data) =>
      val innerGenericType = genericType match {
        case t: ParameterizedType => t.getActualTypeArguments()(0)
      }

      val klass = data.getClass
      val writer = mbw.get
        .getMessageBodyWriter(klass, innerGenericType, annotations, mediaType)
        .asInstanceOf[MessageBodyWriter[Any]]

      writer.writeTo(
        data.asInstanceOf[Any],
        klass,
        innerGenericType,
        annotations,
        mediaType,
        httpHeaders,
        entityStream)
  }

  override def getSize(option: Option[_],
                       rawType: Class[_],
                       genericType: Type,
                       annotations: Array[Annotation],
                       mediaType: MediaType): Long = 0

  override def isWriteable(rawType: Class[_],
                           genericType: Type,
                           annotations: Array[Annotation],
                           mediaType: MediaType): Boolean = {
    classOf[Option[_]].isAssignableFrom(rawType)
  }
}
