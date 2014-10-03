package com.datasift.dropwizard.scala

import com.codahale.metrics.{Counter => JCounter, Histogram => JHistogram, Timer => JTimer}

object metrics {

  implicit final def STimer(t: JTimer) = new STimer(t)
  implicit final def SCounter(c: JCounter) = new SCounter(c)
  implicit final def SHistogram(h: JHistogram) = new SHistogram(h)

  class STimer private[metrics] (t: JTimer) {

    def time[A](f: => A): A = {
      val ctx = t.time()
      val res = f
      ctx.stop()
      res
    }
  }

  class SCounter private[metrics] (c: JCounter) {

    def +=(delta: Long): JCounter = {
      c.inc(delta)
      c
    }

    def -=(delta: Long): JCounter = {
      c.dec(delta)
      c
    }
  }

  class SHistogram private[metrics] (h: JHistogram) {

    def +=(value: Long): JHistogram = {
      h.update(value)
      h
    }

    def +=(value: Int): JHistogram = {
      h.update(value)
      h
    }

    def snapshot = h.getSnapshot
  }
}
