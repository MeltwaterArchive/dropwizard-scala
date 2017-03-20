package com.datasift.dropwizard.scala.jersey.inject

import java.lang.annotation.Annotation
import java.lang.reflect.Type
import javax.inject.Inject
import javax.ws.rs.ext.{ParamConverterProvider, ParamConverter}

import org.glassfish.hk2.api.ServiceLocator

import scala.util.{Failure, Success, Try}

class BigDecimalParamConverter extends ParamConverter[BigDecimal] {
  override def fromString(value: String): BigDecimal = BigDecimal(value)
  override def toString(value: BigDecimal): String = value.toString
}

class BigIntParamConverter extends ParamConverter[BigInt] {
  override def fromString(value: String): BigInt = BigInt(value)
  override def toString(value: BigInt): String = value.toString
}

class EitherParamConverter[L, R](left: ParamConverter[L],
                                 right: ParamConverter[R])
  extends ParamConverter[Either[L, R]] {

  override def fromString(value: String): Either[L, R] = {
    Try(right.fromString(value)) match {
      case Success(v) => Right(v)
      case Failure(_) => Left(left.fromString(value))
    }
  }

  override def toString(value: Either[L, R]): String = value match {
    case Right(v) => right.toString(v)
    case Left(v) => left.toString(v)
  }
}

class OptionParamConverter[A](conv: ParamConverter[A])
  extends ParamConverter[Option[A]] {

  override def fromString(value: String): Option[A] = {
    Option(value).map(conv.fromString)
  }

  override def toString(value: Option[A]): String = {
    value.map(conv.toString).getOrElse("")
  }
}

class ScalaParamConvertersProvider @Inject() (locator: ServiceLocator)
  extends ParamConverterProvider {

  override def getConverter[T](rawType: Class[T],
                               genericType: Type,
                               annotations: Array[Annotation]): ParamConverter[T] = {
    if (rawType == classOf[BigDecimal])
      (new BigDecimalParamConverter).asInstanceOf[ParamConverter[T]]
    else if (rawType == classOf[BigInt])
      (new BigIntParamConverter).asInstanceOf[ParamConverter[T]]
    else if (rawType == classOf[Option[_]]) {
      ParamConverters.getFirstConverter(locator, genericType, annotations)
        .map(new OptionParamConverter(_).asInstanceOf[ParamConverter[T]])
        .orNull
    }
    else if (rawType == classOf[Either[_, _]]) {
      ParamConverters.getConverters(locator, genericType, annotations) match {
        case left :: right :: Nil =>
          new EitherParamConverter(left, right).asInstanceOf[ParamConverter[T]]
        case _ => null
      }
    } else null
  }
}
