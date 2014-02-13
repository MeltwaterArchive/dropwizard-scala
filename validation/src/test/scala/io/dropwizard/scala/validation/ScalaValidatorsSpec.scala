package io.dropwizard.scala.validation

import org.specs2.mutable._
import org.specs2.mock.Mockito
import io.dropwizard.setup.Bootstrap
import io.dropwizard.{Configuration, Application}
import io.dropwizard.scala.validation.constraints._

import scala.collection.JavaConverters.asScalaSetConverter

import javax.validation.Validator

class ScalaValidatorsSpec extends Specification with Mockito {

  case class Fixture (
    @NotEmpty mandatoryOption: Option[Int] = Option(5),
    @NotEmpty mandatoryList: List[Int] = List(1, 2, 3),

    @Size(min = 1, max = 3) shortList: List[Int] = List(1, 2)
  )

  "@NotEmpty Option" should {

    val bootstrap = new Bootstrap[Configuration](mock[Application[Configuration]])

    new ScalaValidatorsBundle().initialize(bootstrap)
    val validator: Validator = bootstrap.getValidatorFactory.getValidator

    "not permit None" in {
      val violations = validator.validate(Fixture(mandatoryOption = None)).asScala
      violations.size must_== 1
    }
    "permit Some" in {
      validator.validate(Fixture(mandatoryOption = Option(5))).asScala must beEmpty
    }
  }

  "@NotEmpty List" should {

    val bootstrap = new Bootstrap[Configuration](mock[Application[Configuration]])

    new ScalaValidatorsBundle().initialize(bootstrap)
    val validator: Validator = bootstrap.getValidatorFactory.getValidator

    "not permit empty List" in {
      val violations = validator.validate(Fixture(mandatoryList = Nil)).asScala
      violations.size must_== 1
    }
    "permit List with elements" in {
      validator.validate(Fixture(mandatoryList = List(5))).asScala must beEmpty
    }
  }

  "@Size List" should {

    val bootstrap = new Bootstrap[Configuration](mock[Application[Configuration]])

    new ScalaValidatorsBundle().initialize(bootstrap)
    val validator: Validator = bootstrap.getValidatorFactory.getValidator

    "permit in-range" in {
      validator.validate(Fixture(shortList = List(1, 2))).asScala must beEmpty
    }

    "not permit too few" in {
      val violations = validator.validate(Fixture(shortList = Nil)).asScala
      violations.size must_== 1
    }

    "not permit too many" in {
      val violations = validator.validate(Fixture(shortList = List(1, 2, 3, 4, 5))).asScala
      violations.size must_== 1
    }
  }
}
