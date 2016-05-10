package com.datasift.dropwizard.scala.jersey.inject

import org.glassfish.jersey.server.internal.inject.MultivaluedParameterExtractor

import collection.generic.CanBuildFrom
import javax.ws.rs.core.MultivaluedMap

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import scala.annotation.unchecked.{uncheckedVariance => uV}
import scala.reflect.ClassTag

object CollectionParameterExtractor {

  def apply[Col[_] <: TraversableOnce[_]]
           (name: String,
            defaultValue: Option[String],
            fromString: String => Any)
           (implicit bf: CanBuildFrom[Nothing, Any, Col[Any @uV]],
                     ct: ClassTag[Col[_]]): MultivaluedParameterExtractor[_] = {
    new CollectionParameterExtractor[Any, Col](name, defaultValue, fromString)
  }
}

/** A parameter extractor for Scala collections with elements decoded by a function.
  *
  * @tparam A type of the elements in the collection.
  * @tparam Col type of the collection to extract.
  * @param name the name of the parameter to extract the collection for.
  * @param defaultValue the default value of the collection for when the parameter does not exist.
  * @param fromString a function to parse collection elements from a string
  * @param bf the implicit builder for the collection type.
  * @see [[MultivaluedParameterExtractor]]
  */
class CollectionParameterExtractor[A, Col[_] <: TraversableOnce[_]]
                                  (name: String,
                                   defaultValue: Option[String],
                                   fromString: String => A)
                                  (implicit bf: CanBuildFrom[Nothing, A, Col[A @uV]])
  extends MultivaluedParameterExtractor[Col[A]] {

  private val default = defaultValue.toIterable

  override def getName = name

  override def getDefaultValueString = defaultValue.orNull

  override def extract(parameters: MultivaluedMap[String, String]): Col[A] = {
    val t = Option(parameters.get(name))
      .map(_.asScala)
      .getOrElse(default)
      .map(fromString)

    val b = bf()
    b.sizeHint(t)
    b ++= t
    b.result()
  }
}
