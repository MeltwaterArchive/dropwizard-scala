package com.datasift.dropwizard.scala.jersey

import java.lang.annotation.Annotation
import java.lang.reflect.{ParameterizedType, Type}
import javax.inject.{Provider, Inject}
import javax.ws.rs.core.MediaType
import javax.ws.rs.ext.MessageBodyWriter

import org.glassfish.jersey.message.MessageBodyWorkers

import scala.reflect.{ClassTag, classTag}

abstract class ParameterizedMessageBodyWriter[A: ClassTag]
  extends MessageBodyWriter[A] {

  @Inject
  private var mbw: Provider[MessageBodyWorkers] = null
  private val klass = classTag[A].runtimeClass

  protected def getWriter(rawClass: Class[_],
                          tpe: Type,
                          annotations: Array[Annotation],
                          mediaType: MediaType): Option[MessageBodyWriter[Any]] = {
    Option(mbw.get.getMessageBodyWriter(
      rawClass.asInstanceOf[Class[Any]],
      tpe,
      annotations,
      mediaType))
  }

  protected def getTypeArgument(tpe: Type, idx: Int): Option[Type] =
    tpe match {
      case t: ParameterizedType => Option(t.getActualTypeArguments()(idx))
      case _ => None
    }

  override def getSize(value: A,
                       rawType: Class[_],
                       genericType: Type,
                       annotations: Array[Annotation],
                       mediaType: MediaType): Long = 0

  override def isWriteable(rawType: Class[_],
                           genericType: Type,
                           annotations: Array[Annotation],
                           mediaType: MediaType): Boolean = {
    klass.isAssignableFrom(rawType)
  }
}
