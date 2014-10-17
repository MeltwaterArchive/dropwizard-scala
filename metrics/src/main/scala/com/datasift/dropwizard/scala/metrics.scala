package com.datasift.dropwizard.scala

import com.codahale.metrics._
import io.dropwizard.util.Duration

import java.util.concurrent.TimeUnit

object metrics {

  implicit final def TimerWrapper(t: Timer) = new TimerWrapper(t)
  implicit final def CounterWrapper(c: Counter) = new CounterWrapper(c)
  implicit final def HistogramWrapper(h: Histogram) = new HistogramWrapper(h)
  implicit final def MetricRegistryWrapper(r: MetricRegistry) = new MetricRegistryWrapper(r)
  implicit final def GaugeWrapper[A](g: Gauge[A]) = new GaugeWrapper[A](g)

  class MetricRegistryWrapper private[metrics] (r: MetricRegistry) {
    
    def gauge[A](name: String)
                (f: => A): Gauge[A] = r.register(name, new Gauge[A] {
      override def getValue = f
    })

    def gauge[A](name: String, timeout: Long, timeoutUnit: TimeUnit)
                      (f: => A): CachedGauge[A] = {
      r.register(name, new CachedGauge[A](timeout, timeoutUnit) {
        override def loadValue() = f
      })
    }

    def gauge[A](name: String, timeout: Duration)
                      (f: => A): CachedGauge[A] = {
      gauge(name, timeout.getQuantity, timeout.getUnit)(f)
    }

    def gauge[A](name: String, clock: Clock, timeout: Long, timeoutUnit: TimeUnit)
                      (f: => A): CachedGauge[A] = {
      r.register(name, new CachedGauge[A](clock, timeout, timeoutUnit) {
        override def loadValue() = f
      })
    }

    def gauge[A](name: String, clock: Clock, timeout: Duration)
                      (f: => A): CachedGauge[A] = {
      gauge(name, clock, timeout.getQuantity, timeout.getUnit)(f)
    }
  }

  class GaugeWrapper[A] private[metrics] (g: Gauge[A]) {

    def map[B](f: A => B): Gauge[B] = new DerivativeGauge[A, B](g) {
      override def transform(value: A): B = f(value)
    }
  }
  
  class TimerWrapper private[metrics] (t: Timer) {

    def timed[A](f: => A): A = {
      val ctx = t.time()
      val res = f
      ctx.stop()
      res
    }
  }

  class CounterWrapper private[metrics] (c: Counter) {

    def +=(delta: Long): Counter = {
      c.inc(delta)
      c
    }

    def -=(delta: Long): Counter = {
      c.dec(delta)
      c
    }
  }

  class HistogramWrapper private[metrics] (h: Histogram) {

    def +=(value: Long): Histogram = {
      h.update(value)
      h
    }

    def +=(value: Int): Histogram = {
      h.update(value)
      h
    }

    def snapshot = h.getSnapshot
  }
}
