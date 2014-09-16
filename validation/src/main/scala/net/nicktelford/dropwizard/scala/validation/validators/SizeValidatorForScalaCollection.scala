package net.nicktelford.dropwizard.scala.validation.validators

import org.hibernate.validator.internal.util.logging.LoggerFactory
import javax.validation.{ConstraintValidatorContext, ConstraintValidator}
import javax.validation.constraints.Size
import scala.collection.GenTraversableOnce

object SizeValidatorForScalaCollection {
  val log = LoggerFactory.make()
}

class SizeValidatorForScalaCollection extends ConstraintValidator[Size, GenTraversableOnce[_]] {

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

  def isValid(value: GenTraversableOnce[_], context: ConstraintValidatorContext): Boolean = {
    if (value == null) {
      true
    } else {
      val size = value.size
      size >= min && size <= max
    }
  }
}
