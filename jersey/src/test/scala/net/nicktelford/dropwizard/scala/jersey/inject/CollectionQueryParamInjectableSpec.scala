package net.nicktelford.dropwizard.scala.jersey.inject

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.{ eq => is }

import com.sun.jersey.server.impl.model.parameter.multivalued.MultivaluedParameterExtractor
import com.sun.jersey.api.core.{ExtendedUriInfo, HttpContext}
import javax.ws.rs.core.MultivaluedMap

/**
 * Tests [[net.nicktelford.dropwizard.scala.jersey.inject.CollectionQueryParamInjectable]]
 */
class CollectionQueryParamInjectableSpec extends FlatSpec with MockitoSugar {

  val extractor = mock[MultivaluedParameterExtractor]
  val context = mock[HttpContext]
  val uriInfo = mock[ExtendedUriInfo]
  val params = mock[MultivaluedMap[String, String]]
  val extracted = new Object

  when(context.getUriInfo) thenReturn uriInfo
  when(extractor.extract(is(params))) thenReturn (extracted, Nil: _*)

  "A decoding ScalaCollectionQueryParamInjectable" should "extract query parameters" in {
    val injectable = new CollectionQueryParamInjectable(extractor, true)
    when(uriInfo.getQueryParameters(is(true))) thenReturn params
    assert(injectable.getValue(context) === extracted)
    verify(uriInfo).getQueryParameters(true)
  }

  "A non-decoding ScalaCollectionQueryParamInjectable" should "extract query parameters" in {
    val injectable = new CollectionQueryParamInjectable(extractor, false)
    when(uriInfo.getQueryParameters(is(false))) thenReturn params
    assert(injectable.getValue(context) === extracted)
    verify(uriInfo).getQueryParameters(false)
  }
}
