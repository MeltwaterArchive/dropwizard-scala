package com.datasift.dropwizard.scala.jdbi.tweak

import scala.language.higherKinds

import org.skife.jdbi.v2.ContainerBuilder
import org.skife.jdbi.v2.tweak.ContainerFactory
import scala.collection.generic.CanBuildFrom
import scala.reflect.ClassTag

/** A [[org.skife.jdbi.v2.tweak.ContainerFactory]] for Scala collections.
  *
  * @tparam CC the collection type to build.
  * @param tag type tag for collection for reification of generic type.
  * @param cbf functional dependency for collection builder.
  */
class IterableContainerFactory[CC[_] <: Iterable[_]]
    (implicit tag: ClassTag[CC[_]], cbf: CanBuildFrom[CC[_], Any, CC[Any]])
  extends ContainerFactory[CC[Any]] {

  def accepts(clazz: Class[_]): Boolean = tag.runtimeClass.isAssignableFrom(clazz)

  def newContainerBuilderFor(clazz: Class[_]): ContainerBuilder[CC[Any]] = {
    new ContainerBuilder[CC[Any]] {

      val builder = cbf()

      def add(it: Any): ContainerBuilder[CC[Any]] = {
        builder += it
        this
      }

      def build(): CC[Any] = builder.result()
    }
  }
}
