package net.nicktelford.dropwizard.scala.jdbi.tweak

import org.scalatest.FlatSpec

/**
 * Tests [[net.nicktelford.dropwizard.scala.jdbi.tweak.OptionContainerFactory]]
 */
class OptionContainerFactorySpec extends FlatSpec {

  val factory = new OptionContainerFactory

  "OptionContainerFactory for Ints" should "Accepts Options" in {
    assert(factory.accepts(classOf[Option[Int]]))
  }

  it should "not accept Lists" in {
    assert(!factory.accepts(classOf[List[Int]]))
  }

  it should "build a None by default" in {
  assert(factory.newContainerBuilderFor(classOf[Int])
      .build() === None)
  }

  it should "Builds a Some of an Int on demand" in {
    assert(factory.newContainerBuilderFor(classOf[Int]).add(123)
      .build() === Some(123))
  }

  it should "Builds a Some of the last Int on demand" in {
    assert(factory.newContainerBuilderFor(classOf[Int]).add(123).add(456)
      .build() === Some(456))
  }


  "OptionContainerFactory for Strings" should "accept Options" in {
    assert(factory.accepts(classOf[Option[String]]))
  }

  it should "Doesn't accept Lists" in {
    assert(!factory.accepts(classOf[List[String]]))
  }

  it should "Builds a None by default" in {
    assert(factory.newContainerBuilderFor(classOf[String])
      .build() === None)
  }

  it should "Builds a Some of a String on demand" in {
    assert(factory.newContainerBuilderFor(classOf[String]).add("abc")
      .build() === Some("abc"))
  }

  it should "Builds a Some of the last String on demand" in {
    assert(factory.newContainerBuilderFor(classOf[String]).add("abc").add("def")
      .build() === Some("def"))
  }
}
