package com.datasift.dropwizard.scala.jersey.inject

import org.scalatest.FlatSpec

import com.sun.jersey.core.util.MultivaluedMapImpl

/** Tests for [[com.datasift.dropwizard.scala.jersey.inject.CollectionParameterExtractor]]. */
class CollectionParameterExtractorSpec extends FlatSpec {

  "StringCollectionParameterExtractor with default" should "have a name" in {
    val extractor = new StringCollectionParameterExtractor[Set]("name", "default")
    assert(extractor.getName === "name")
  }

  it should "extract parameter values" in {
    val extractor = new StringCollectionParameterExtractor[Set]("name", "default")
    val params = new MultivaluedMapImpl()
    params.add("name", "one")
    params.add("name", "two")
    params.add("name", "three")

    assert(extractor.extract(params) === Set("one", "two", "three"))
  }

  it should "uses default if no parameter exists" in {
    val extractor = new StringCollectionParameterExtractor[Set]("name", "default")
    assert(extractor.extract(new MultivaluedMapImpl()) === Set("default"))
  }

  "Extractor without default" should "return empty collection" in {
    val extractor = new StringCollectionParameterExtractor[Set]("name", null)
    assert(extractor.extract(new MultivaluedMapImpl()) === Set.empty)
  }
}
