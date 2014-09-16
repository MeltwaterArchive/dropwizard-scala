package net.nicktelford.dropwizard.scala.validation

import io.dropwizard.Bundle
import io.dropwizard.setup.{Bootstrap, Environment}
import net.nicktelford.dropwizard.scala.validation.validators._
import org.hibernate.validator.internal.engine.ValidatorFactoryImpl
import org.hibernate.validator.internal.metadata.core.ConstraintHelper

import scala.reflect.{ClassTag, classTag}
import scala.collection.JavaConverters.seqAsJavaListConverter

import java.util
import java.lang.annotation.Annotation
import javax.validation.ConstraintValidator
import javax.validation.constraints.Size

object ScalaValidatorsBundle {

  val sizeValidators = List(
    classOf[SizeValidatorForScalaCollection],
    classOf[SizeValidatorForScalaOption])
}

/** Adds support for Scala collections to the validation framework. */
class ScalaValidatorsBundle extends Bundle {

  import ScalaValidatorsBundle._

  def initialize(bootstrap: Bootstrap[_]) {
    bootstrap.getValidatorFactory match {
      case factory: ValidatorFactoryImpl => {
        // we need to resort to reflection here to get access to the ConstraintHelper, where all the magic happens
        val constraintHelperMethod = classOf[ValidatorFactoryImpl].getDeclaredField("constraintHelper")
        constraintHelperMethod.setAccessible(true)
        val constraintHelper = constraintHelperMethod.get(factory).asInstanceOf[ConstraintHelper]

        // add custom constraint mappings
        addValidators[Size](constraintHelper, sizeValidators)
      }
      case _ => // ignore unrecognized implementations
    }
  }

  def run(environment: Environment) {

  }

  private def addValidators[A <: Annotation : ClassTag](helper: ConstraintHelper,
                                                        validators: List[Class[_ <: ConstraintValidator[A, _]]]) {
    val annoClass = classTag[A].runtimeClass.asInstanceOf[Class[A]]
    val allValidators = new util.LinkedList[Class[_ <: ConstraintValidator[A, _]]](validators.asJava)

    // ensure we don't replace existing validators
    allValidators.addAll(helper.getAllValidatorClasses(annoClass))

    helper.putValidatorClasses(annoClass, allValidators, false)
  }
}
