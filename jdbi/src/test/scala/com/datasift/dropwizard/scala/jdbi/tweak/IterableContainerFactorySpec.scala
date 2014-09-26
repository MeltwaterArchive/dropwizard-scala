package com.datasift.dropwizard.scala.jdbi.tweak

import org.scalatest.FlatSpec

import scala.collection.immutable.{SortedSet, HashSet}

/**
 * Tests [[com.datasift.dropwizard.scala.jdbi.tweak.IterableContainerFactory]]
 */
class IterableContainerFactorySpec extends FlatSpec {

  "IterableContainerFactory for Seqs" should "accept Seqs" in {
    assert(new IterableContainerFactory[Seq].accepts(classOf[Seq[Int]]))
  }

  it should "accept Lists" in {
    assert(new IterableContainerFactory[Seq].accepts(classOf[List[Int]]))
  }

  it should "accept Vectors" in {
    assert(new IterableContainerFactory[Seq].accepts(classOf[Vector[Int]]))
  }

  it should "build an empty Seq" in {
    assert(new IterableContainerFactory[Seq].newContainerBuilderFor(classOf[Int])
      .build().isEmpty)
  }

  it should "build a Seq of Ints on demand" in {
    assert(new IterableContainerFactory[Seq].newContainerBuilderFor(classOf[Int]).add(123)
      .build() === Seq(123))
  }

  it should "build a Seq of Strings on demand" in {
    assert(new IterableContainerFactory[Seq].newContainerBuilderFor(classOf[String]).add("abc").add("def")
      .build() === Seq("abc", "def"))
  }


  "IterableContainerFactory for Sets" should "accept Sets" in {
    assert(new IterableContainerFactory[Set].accepts(classOf[Set[Int]]))
  }

  it should "accept Lists" in {
    assert(new IterableContainerFactory[Set].accepts(classOf[SortedSet[Int]]))
  }

  it should "accept Vectors" in {
    assert(new IterableContainerFactory[Set].accepts(classOf[HashSet[Int]]))
  }

  it should "build an empty Set" in {
    assert(new IterableContainerFactory[Set].newContainerBuilderFor(classOf[Int])
      .build().isEmpty)
  }

  it should "build a Set of Ints on demand" in {
    assert(new IterableContainerFactory[Set].newContainerBuilderFor(classOf[Int]).add(123)
      .build() === Set(123))
  }

  it should "build a Set of Strings on demand" in {
    assert(new IterableContainerFactory[Set].newContainerBuilderFor(classOf[String]).add("abc").add("def")
      .build() === Set("abc", "def"))
  }
}
