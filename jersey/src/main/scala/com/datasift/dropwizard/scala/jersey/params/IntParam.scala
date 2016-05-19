package com.datasift.dropwizard.scala.jersey.params

import io.dropwizard.jersey.params.AbstractParam

/** Factory object for [[io.dropwizard.jersey.params.IntParam]]. */
object IntParam {

  /** Creates a parameter extractor for the given value. */
  def apply(value: Int): AbstractParam[Int] = IntParam(value.toString)
}

/** Parameter extractor for [[scala.Int]].
  *
  * @param s the input data to extract the [[scala.Int]] from.
  *
  * @see [[io.dropwizard.jersey.params.AbstractParam]]
  */
case class IntParam(s: String) extends AbstractParam[Int](s) {

  protected def parse(input: String) = s.toInt
}
