package com.datasift.dropwizard.scala

import com.codahale.metrics._
import com.datasift.dropwizard.scala.metrics._
import org.scalatest.FlatSpec

class MetricImplicitsSpec extends FlatSpec {

  "Timer" should "time execution of a function" in {

    object TestClock extends Clock {
      var tick = 0
      override def getTick = tick
    }

    val timer = new Timer(new UniformReservoir(), TestClock)
    timer.time { TestClock.tick += 1 }

    assert(TestClock.tick === 1)
  }

  "Counter" should "increment using +=" in {
    val counter = new Counter()
    counter += 250
    assert(counter.getCount === 250)
  }

  it should "decrement using -=" in {
    val counter = new Counter()
    counter.inc(1000)
    counter -= 250
    assert(counter.getCount === 750)
  }

  it should "chain += and -=" in {
    val counter = new Counter()
    counter += 500 -= 125
    assert(counter.getCount === 375)
  }

  "Histogram" should "add Longs with +=" in {
    val histogram = new Histogram(new UniformReservoir())
    val long = Int.MaxValue.toLong * 200
    histogram += long
    assert(histogram.getCount === 1)
    assert(histogram.getSnapshot.getValues === Array(long))
  }

  it should "add Ints with +=" in {
    val histogram = new Histogram(new UniformReservoir())
    val int = 5678
    histogram += int
    assert(histogram.getCount === 1)
    assert(histogram.getSnapshot.getValues === Array(int))
  }
}
