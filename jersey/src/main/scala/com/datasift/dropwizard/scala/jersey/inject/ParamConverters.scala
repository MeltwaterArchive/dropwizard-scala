package com.datasift.dropwizard.scala.jersey.inject

import java.lang.annotation.Annotation
import java.lang.reflect.Type
import javax.ws.rs.ext.{ParamConverterProvider, ParamConverter}

import org.glassfish.hk2.api.ServiceLocator
import org.glassfish.jersey.internal.inject.Providers
import org.glassfish.jersey.internal.util.ReflectionHelper
import org.glassfish.jersey.internal.util.collection.ClassTypePair

import collection.JavaConverters._

object ParamConverters {

  object Identity extends ParamConverter[String] {
    override def fromString(value: String): String = value
    override def toString(value: String): String = value
  }

  def getConverter(locator: ServiceLocator,
                   ctp: ClassTypePair,
                   annotations: Array[Annotation]): Option[ParamConverter[_]] = {
    if (ctp.rawClass == classOf[String]) {
      Option(Identity)
    } else {
      Providers.getProviders(locator, classOf[ParamConverterProvider]).asScala
        .flatMap { provider =>
          Option(provider.getConverter(ctp.rawClass, ctp.`type`, annotations)
            .asInstanceOf[ParamConverter[Any]]
          )
        }
        .headOption
    }
  }

  def getFirstConverter(locator: ServiceLocator,
                        tpe: Type,
                        annotations: Array[Annotation]): Option[ParamConverter[_]] = {
    ReflectionHelper.getTypeArgumentAndClass(tpe)
      .asScala
      .headOption
      .flatMap(getConverter(locator, _, annotations))
  }

  def getConverters(locator: ServiceLocator,
                    ctps: Seq[ClassTypePair],
                    annotations: Array[Annotation]): List[ParamConverter[_]] = {
    ctps.flatMap(getConverter(locator, _, annotations)).toList
  }

  def getConverters(locator: ServiceLocator,
                    tpe: Type,
                    annotations: Array[Annotation]): List[ParamConverter[_]] = {
    val args = ReflectionHelper.getTypeArgumentAndClass(tpe).asScala.toList
    val conv = getConverters(locator, args, annotations)
    if (conv.size == args.size) conv
    else Nil
  }

}
