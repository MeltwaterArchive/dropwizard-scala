package com.datasift.dropwizard.scala.jersey.inject

import org.scalatest.FlatSpec

import com.sun.jersey.core.util.MultivaluedMapImpl

/** Tests for [[com.datasift.dropwizard.scala.jersey.inject.OptionParameterExtractor]]. */
class OptionParameterExtractorSpec extends FlatSpec {

  "Extracting a parameter" should "have a name" in {
    val extractor = new OptionParameterExtractor("name", "default")
    assert(extractor.getName === "name")
  }

  it should "have a default value" in {
    val extractor = new OptionParameterExtractor("name", "default")
    assert(extractor.getDefaultStringValue === "default")
  }

  it should "extract the first value from a set of parameter values" in {
    val extractor = new OptionParameterExtractor("name", "default")
    val params = new MultivaluedMapImpl()
    params.add("name", "one")
    params.add("name", "two")
    params.add("name", "three")

    assert(extractor.extract(params) === Some("one"))
  }

  it should "uses the default value if no parameter exists" in {
    val extractor = new OptionParameterExtractor("name", "default")
    val params = new MultivaluedMapImpl()

    assert(extractor.extract(params) === Some("default"))
  }

  "Extracting a parameter without a default value" should  "return None if no parameter exists" in {
    val extractor = new OptionParameterExtractor("name", null)
    assert(extractor.extract(new MultivaluedMapImpl()) === None)
  }
}
