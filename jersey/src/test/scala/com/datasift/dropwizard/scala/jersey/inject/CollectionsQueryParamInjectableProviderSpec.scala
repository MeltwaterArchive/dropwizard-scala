package com.datasift.dropwizard.scala.jersey.inject

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.mockito.Mockito._
import org.mockito.Matchers.any

import com.sun.jersey.api.core.{ExtendedUriInfo, HttpContext}
import com.sun.jersey.core.util.MultivaluedMapImpl
import com.sun.jersey.core.spi.component.{ComponentScope, ComponentContext}
import com.sun.jersey.api.model.Parameter
import javax.ws.rs.QueryParam

/** Tests for
  * [[com.datasift.dropwizard.scala.jersey.inject.CollectionsQueryParamInjectableProvider]].
  */
class CollectionsQueryParamInjectableProviderSpec extends FlatSpec with MockitoSugar {

  val httpContext = mock[HttpContext]
  val uriInfo = mock[ExtendedUriInfo]
  val params = new MultivaluedMapImpl()
  params.add("name", "one")
  params.add("name", "two")
  params.add("name", "three")

  when(uriInfo.getQueryParameters(any[Boolean])) thenReturn params
  when(httpContext.getUriInfo) thenReturn uriInfo

  val context = mock[ComponentContext]

  val provider = new CollectionsQueryParamInjectableProvider()

  object queryParam {
    private def mock(@QueryParam("x") x: String) = x
    def annotation = getClass.getDeclaredMethod("mock", classOf[String]).getAnnotation(classOf[QueryParam])
  }

  "ScalaCollectionsQueryParamInjectableProvider" should "have a per-request scope" in {
    assert(provider.getScope === ComponentScope.PerRequest)
  }

  it should "return an injectable for Seq instances" in {
    val param = new Parameter(Array(), null, null, "name", null, classOf[Seq[String]], false, "default")
    val injectable = provider.getInjectable(context, queryParam.annotation, param)

    assert(injectable.getValue(httpContext) === Seq("one", "two", "three"))
  }

  it should "return an injectable for List instances" in {
    val param = new Parameter(Array(), null, null, "name", null, classOf[List[String]], false, "default")
    val injectable = provider.getInjectable(context, queryParam.annotation, param)

    assert(injectable.getValue(httpContext) === List("one", "two", "three"))
  }

  it should "return an injectable for Vector instances" in {
    val param = new Parameter(Array(), null, null, "name", null, classOf[Vector[String]], false, "default")
    val injectable = provider.getInjectable(context, queryParam.annotation, param)

    assert(injectable.getValue(httpContext) === Vector("one", "two", "three"))
  }

  it should "return an injectable for IndexedSeq instances" in {
    val param = new Parameter(Array(), null, null, "name", null, classOf[IndexedSeq[String]], false, "default")
    val injectable = provider.getInjectable(context, queryParam.annotation, param)

    assert(injectable.getValue(httpContext) === IndexedSeq("one", "two", "three"))
  }

  it should "return an injectable for Set instances" in {
    val param = new Parameter(Array(), null, null, "name", null, classOf[Set[String]], false, "default")
    val injectable = provider.getInjectable(context, queryParam.annotation, param)

    assert(injectable.getValue(httpContext) === Set("one", "two", "three"))
  }

  it should "return an injectable for Option instances" in {
    val param = new Parameter(Array(), null, null, "name", null, classOf[Option[String]], false, "default")
    val injectable = provider.getInjectable(context, queryParam.annotation, param)

    assert(injectable.getValue(httpContext) === Option("one"))
  }
}
