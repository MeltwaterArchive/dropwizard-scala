package io.dropwizard.scala.validation.validators

import org.hibernate.validator.internal.util.logging.LoggerFactory
import javax.validation.{ConstraintValidatorContext, ConstraintValidator}
import javax.validation.constraints.Size

object SizeValidatorForScalaOption {
  val log = LoggerFactory.make()
}

class SizeValidatorForScalaOption extends ConstraintValidator[Size, Option[_]] {

  import SizeValidatorForScalaCollection.log

  var min = 0
  var max = 0

  def initialize(parameters: Size) {
    min = parameters.min()
    max = parameters.max()

    if (min < 0) throw log.getMinCannotBeNegativeException
    if (max < 0) throw log.getMaxCannotBeNegativeException
    if (max < min) throw log.getLengthCannotBeNegativeException
  }

  def isValid(value: Option[_], context: ConstraintValidatorContext): Boolean = value match {
    case null => true
    case None if min == 0 => true
    case Some(_) if max > 0 => true
    case _ => false
  }
}
