package com.datasift.dropwizard.scala

import com.codahale.metrics._
import com.datasift.dropwizard.scala.metrics._
import io.dropwizard.util.Duration
import org.scalatest.FlatSpec

import scala.collection.JavaConverters.mapAsScalaMapConverter

class MetricImplicitsSpec extends FlatSpec {

  class TestClock(var tick: Long = 0) extends Clock {
    override def getTick = tick
  }

  "Timer" should "time execution of a function" in {


    val clock = new TestClock(0)
    val timer = new Timer(new UniformReservoir(), clock)
    timer.timed { clock.tick += 1 }

    assert(clock.tick === 1)
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

  "MetricRegistry" should "register a gauge for an arbitrary function" in {
    val registry = new MetricRegistry
    var value = 5
    val gauge = registry.gauge("test.gauge")(value)
    assert(registry.getGauges.asScala === Map("test.gauge" -> gauge))
    assert(gauge.getValue === value)
    value = 15
    assert(gauge.getValue === value)
  }

  it should "register a cached gauge for an arbitrary function" in {
    val registry = new MetricRegistry
    var value = 5
    val clock = new TestClock(0)
    val gauge = registry.gauge("test.gauge", clock, Duration.nanoseconds(5))(value)
    assert(registry.getGauges.asScala === Map("test.gauge" -> gauge))
    assert(gauge.getValue === value)
    val oldValue = value
    value = 50
    assert(gauge.getValue === oldValue)
    clock.tick = 4
    assert(gauge.getValue === oldValue)
    clock.tick = 5
    assert(gauge.getValue === value)
  }

  "Gauge" should "be transformable by another function" in {
    val registry = new MetricRegistry
    val gauge = registry.gauge("test.gauge")(50)
    val transformed = gauge.map(2 * _)
    assert(gauge.getValue === 50)
    assert(transformed.getValue === 100)
  }
}
