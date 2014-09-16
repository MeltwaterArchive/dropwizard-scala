package net.nicktelford.dropwizard.scala.jersey.params

import io.dropwizard.jersey.params.AbstractParam

/** Factory object for [[io.dropwizard.jersey.params.LongParam]]. */
object LongParam {

  /** Creates a parameter extractor for the given value. */
  def apply(value: Long): AbstractParam[Long] = LongParam(value.toString)
}

/** Parameter extractor for [[scala.Long]].
  *
  * @param s the input data to extract the [[scala.Long]] from.
  *
  * @see [[io.dropwizard.jersey.params.AbstractParam]]
  */
case class LongParam(s: String) extends AbstractParam[Long](s) {
  protected def parse(input: String) = s.toLong

  override protected def errorMessage(input: String, e: Exception) = {
    "Invalid parameter: %s (Must be an integer value.)".format(input)
  }
}
