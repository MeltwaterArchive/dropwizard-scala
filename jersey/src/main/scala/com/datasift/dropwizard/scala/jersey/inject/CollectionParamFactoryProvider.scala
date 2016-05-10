package com.datasift.dropwizard.scala.jersey.inject

import javax.inject.Inject

import org.glassfish.hk2.api.{Factory, ServiceLocator}
import org.glassfish.jersey.server.internal.inject._
import org.glassfish.jersey.server.model.Parameter

abstract class CollectionParamFactoryProvider @Inject()(mpep: MultivaluedParameterExtractorProvider,
                                                        locator: ServiceLocator,
                                                        source: Parameter.Source)
  extends AbstractValueFactoryProvider(mpep, locator, source) {

  override def createValueFactory(parameter: Parameter): Factory[_] = {
    val name = Option(parameter.getSourceName)
    val defaultValue = Option(parameter.getDefaultValue)
    val tpe = parameter.getType
    val klass = parameter.getRawType
    val annotations = parameter.getAnnotations

    name.filter(_.nonEmpty).flatMap { name =>
      ParamConverters.getFirstConverter(locator, tpe, annotations)
        .map(x => x.fromString(_))
        .flatMap(buildExtractor(klass, name, defaultValue, _))
        .map(buildFactory(_, !parameter.isEncoded))
    }.orNull
  }

  protected def buildFactory(mpe: MultivaluedParameterExtractor[_],
                             decode: Boolean): AbstractContainerRequestValueFactory[_]

  private def buildExtractor(klass: Class[_],
                             name: String,
                             defaultValue: Option[String],
                             conv: String => Any): Option[MultivaluedParameterExtractor[_]] = Option {
    if (klass == classOf[Seq[_]])
      CollectionParameterExtractor[Seq](name, defaultValue, conv)
    else if (klass == classOf[List[_]])
      CollectionParameterExtractor[List](name, defaultValue, conv)
    else if (klass == classOf[Vector[_]])
      CollectionParameterExtractor[Vector](name, defaultValue, conv)
    else if (klass == classOf[IndexedSeq[_]])
      CollectionParameterExtractor[IndexedSeq](name, defaultValue, conv)
    else if (klass == classOf[Set[_]])
      CollectionParameterExtractor[Set](name, defaultValue, conv)
    else null
  }
}

class CollectionQueryParamFactoryProvider @Inject()(mpep: MultivaluedParameterExtractorProvider,
                                                    locator: ServiceLocator)
  extends CollectionParamFactoryProvider(mpep, locator, Parameter.Source.QUERY) {
  override protected def buildFactory(extractor: MultivaluedParameterExtractor[_],
                                      decode: Boolean): AbstractContainerRequestValueFactory[_] = {
    new QueryParamValueFactory(extractor, decode)
  }
}

class CollectionHeaderParamFactoryProvider @Inject()(mpep: MultivaluedParameterExtractorProvider,
                                                     locator: ServiceLocator)
  extends CollectionParamFactoryProvider(mpep, locator, Parameter.Source.HEADER) {
  override protected def buildFactory(extractor: MultivaluedParameterExtractor[_],
                                      decode: Boolean): AbstractContainerRequestValueFactory[_] = {
    new HeaderParamValueFactory(extractor, decode)
  }
}

class CollectionFormParamFactoryProvider @Inject()(mpep: MultivaluedParameterExtractorProvider,
                                                   locator: ServiceLocator)
  extends CollectionParamFactoryProvider(mpep, locator, Parameter.Source.FORM) {
  override protected def buildFactory(extractor: MultivaluedParameterExtractor[_],
                                      decode: Boolean): AbstractContainerRequestValueFactory[_] = {
    new FormParamValueFactory(extractor, decode)
  }
}
