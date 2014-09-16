package net.nicktelford.dropwizard.scala.jersey.inject

import com.sun.jersey.spi.inject.InjectableProvider
import javax.ws.rs.QueryParam
import com.sun.jersey.api.model.Parameter
import com.sun.jersey.core.spi.component.{ComponentScope, ComponentContext}
import javax.ws.rs.ext.Provider

/** Provides query parameter extractors for each type of Scala collection.
  *
  * @see [[net.nicktelford.dropwizard.scala.jersey.inject.CollectionQueryParamInjectable]]
  * @see [[net.nicktelford.dropwizard.scala.jersey.inject.CollectionParameterExtractor]]
  * @see [[net.nicktelford.dropwizard.scala.jersey.inject.OptionParameterExtractor]]
  */
@Provider
class CollectionsQueryParamInjectableProvider
  extends InjectableProvider[QueryParam, Parameter] {

  def getScope = ComponentScope.PerRequest

  def getInjectable(context: ComponentContext, queryParam: QueryParam, param: Parameter) = {
    val name = param.getSourceName
    val default = param.getDefaultValue
    val clazz = param.getParameterClass

    if (name != null && !name.isEmpty) {
      val ex = if (clazz == classOf[Seq[_]]) {
          new StringCollectionParameterExtractor[Seq](name, default)
        } else if (clazz == classOf[List[_]]) {
          new StringCollectionParameterExtractor[List](name, default)
        } else if (clazz == classOf[Vector[_]]) {
          new StringCollectionParameterExtractor[Vector](name, default)
        } else if (clazz == classOf[IndexedSeq[_]]) {
          new StringCollectionParameterExtractor[IndexedSeq](name, default)
        } else if (clazz == classOf[Set[_]]) {
          new StringCollectionParameterExtractor[Set](name, default)
        } else if (clazz == classOf[Option[_]]) {
          new OptionParameterExtractor(name, default)
        } else {
          null
        }

      if (ex != null) {
        new CollectionQueryParamInjectable(ex, !param.isEncoded)
      } else {
        null
      }
    } else {
      null
    }
  }
}
