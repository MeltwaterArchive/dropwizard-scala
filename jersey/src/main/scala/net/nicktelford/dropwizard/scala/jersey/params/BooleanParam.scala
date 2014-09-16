package net.nicktelford.dropwizard.scala.jersey.params

import io.dropwizard.jersey.params.AbstractParam

/** Factory object for [[io.dropwizard.jersey.params.BooleanParam]]. */
object BooleanParam {

  /** Creates a parameter extractor for the given value. */
  def apply(value: Boolean): AbstractParam[Boolean] = BooleanParam(value.toString)
}

/** Parameter extractor for [[scala.Boolean]].
  *
  * @param s the input data to extract the [[scala.Boolean]] from.
  *
  * @see [[io.dropwizard.jersey.params.AbstractParam]]
  */
case class BooleanParam(s: String) extends AbstractParam[Boolean](s) {

  protected def parse(input: String) = input.toBoolean

  override protected def errorMessage(input: String, e: Exception) = {
    "Invalid parameter: %s (Must be \"true\" or \"false\".)".format(input)
  }
}
