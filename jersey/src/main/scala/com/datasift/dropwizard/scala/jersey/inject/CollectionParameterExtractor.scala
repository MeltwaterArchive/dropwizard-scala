package com.datasift.dropwizard.scala.jersey.inject

import language.higherKinds

import collection.generic.CanBuildFrom
import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor
import javax.ws.rs.core.MultivaluedMap

import scala.collection.JavaConverters.iterableAsScalaIterableConverter
import com.sun.jersey.spi.StringReader
import scala.annotation.unchecked.{uncheckedVariance => uV}

/** A parameter extractor for Scala collections with elements decoded by a function.
  *
  * @tparam A type of the elements in the collection.
  * @tparam Col type of the collection to extract.
  * @param name the name of the parameter to extract the collection for.
  * @param defaultValue the default value of the collection for when the parameter does not exist.
  * @param fromString a function to parse collection elements from a string
  * @param bf the implicit builder for the collection type.
  *
  * @see [[com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor]]
  */
class CollectionParameterExtractor[A, Col[_] <: TraversableOnce[_]]
                                  (name: String, defaultValue: String, fromString: String => A)
                                  (implicit bf: CanBuildFrom[Nothing, A, Col[A @uV]])
  extends MultivaluedParameterExtractor {

  private val default = Option(defaultValue).toIterable

  def getName = name

  def getDefaultStringValue = defaultValue

  def extract(parameters: MultivaluedMap[String, String]): Object = {
    Option(parameters.get(name))
      .map(_.asScala)
      .getOrElse(default)
      .map(fromString)
      .to[Col](bf)
      .asInstanceOf[Object]
  }
}

/** A parameter extractor for Scala collections with elements decoded by a Jersey StringReader.
 *
 * @param name the name of the parameter to extract the collection for.
 * @param defaultValue the default value of the collection for when the parameter does not exist.
 * @param sr the StringReader to parse collection elements.
 * @param bf the implicit builder for the collection type.
 *
 * @tparam A type of the elements in the collection.
 * @tparam Col type of the collection to extract.
 */
class StringReaderCollectionParameterExtractor[A, Col[_] <: Iterable[_]]
                                              (name: String, defaultValue: String, sr: StringReader[A])
                                              (implicit bf: CanBuildFrom[Nothing, A, Col[A @uV]])
  extends CollectionParameterExtractor[A, Col](name, defaultValue, sr.fromString)(bf)

/** A parameter extractor for Scala collections with String elements.
  *
 * @param name the name of the parameter to extract the collection for.
 * @param defaultValue the default value of the collection for when the parameter does not exist.
 * @param bf the implicit builder for the collection type.
 *
 * @tparam Col type of the collection to extract.
 */
class StringCollectionParameterExtractor[Col[_] <: Iterable[_]]
                                        (name: String, defaultValue: String)
                                        (implicit bf: CanBuildFrom[Nothing, String, Col[String @uV]])
  extends CollectionParameterExtractor[String, Col](name, defaultValue, identity)(bf)
