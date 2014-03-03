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

    @Size(min = 1, max = 3) shortList: List[Int] = List(1, 2),

    @AssertTrue beTrue: Boolean = true,
    @AssertFalse beFalse: Boolean = false,

    @Min(5) minInt: Int = 10,
    @Max(15) maxInt: Int = 10,

    @Min(5) minBigInt: BigInt = BigInt(10),
    @Max(15) maxBigInt: BigInt = BigInt(10),

    @DecimalMin("5") minDecimalBigInt: BigInt = BigInt(10),
    @DecimalMax("15") maxDecimalBigInt: BigInt = BigInt(10)
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

  "@AssertTrue Boolean" should {

    val bootstrap = new Bootstrap[Configuration](mock[Application[Configuration]])

    new ScalaValidatorsBundle().initialize(bootstrap)
    val validator: Validator = bootstrap.getValidatorFactory.getValidator

    "permit true" in {
      validator.validate(Fixture(beTrue = true)).asScala must beEmpty
    }

    "not permit false" in {
      validator.validate(Fixture(beTrue = false)).asScala.size must_== 1
    }
  }

  "@AssertFalse Boolean" should {

    val bootstrap = new Bootstrap[Configuration](mock[Application[Configuration]])

    new ScalaValidatorsBundle().initialize(bootstrap)
    val validator: Validator = bootstrap.getValidatorFactory.getValidator

    "permit false" in {
      validator.validate(Fixture(beFalse = false)).asScala must beEmpty
    }

    "not permit true" in {
      validator.validate(Fixture(beFalse = true)).asScala.size must_== 1
    }
  }

  "@Min Int" should {

    val bootstrap = new Bootstrap[Configuration](mock[Application[Configuration]])

    new ScalaValidatorsBundle().initialize(bootstrap)
    val validator: Validator = bootstrap.getValidatorFactory.getValidator

    "permit equal" in {
      validator.validate(Fixture(minInt = 5)).asScala must beEmpty
    }

    "permit larger" in {
      validator.validate(Fixture(minInt = 500)).asScala must beEmpty
    }

    "not permit smaller" in {
      validator.validate(Fixture(minInt = 4)).asScala.size must_== 1
    }
  }

  "@Max Int" should {

    val bootstrap = new Bootstrap[Configuration](mock[Application[Configuration]])

    new ScalaValidatorsBundle().initialize(bootstrap)
    val validator: Validator = bootstrap.getValidatorFactory.getValidator

    "permit equal" in {
      validator.validate(Fixture(maxInt = 15)).asScala must beEmpty
    }

    "permit smaller" in {
      validator.validate(Fixture(maxInt = 5)).asScala must beEmpty
    }

    "not permit larger" in {
      validator.validate(Fixture(maxInt = 500)).asScala.size must_== 1
    }
  }

  "@Min BigInt" should {

    val bootstrap = new Bootstrap[Configuration](mock[Application[Configuration]])

    new ScalaValidatorsBundle().initialize(bootstrap)
    val validator: Validator = bootstrap.getValidatorFactory.getValidator

    "permit equal" in {
      validator.validate(Fixture(minBigInt = BigInt(5))).asScala must beEmpty
    }

    "permit larger" in {
      validator.validate(Fixture(minBigInt = BigInt(500))).asScala must beEmpty
    }

    "not permit smaller" in {
      validator.validate(Fixture(minBigInt = BigInt(4))).asScala.size must_== 1
    }
  }
  "@Max BigInt" should {

    val bootstrap = new Bootstrap[Configuration](mock[Application[Configuration]])

    new ScalaValidatorsBundle().initialize(bootstrap)
    val validator: Validator = bootstrap.getValidatorFactory.getValidator

    "permit equal" in {
      validator.validate(Fixture(maxBigInt = BigInt(15))).asScala must beEmpty
    }

    "permit smaller" in {
      validator.validate(Fixture(maxBigInt = BigInt(5))).asScala must beEmpty
    }

    "not permit larger" in {
      validator.validate(Fixture(maxBigInt = BigInt(500))).asScala.size must_== 1
    }
  }

  "@DecimalMin BigInt" should {

    val bootstrap = new Bootstrap[Configuration](mock[Application[Configuration]])

    new ScalaValidatorsBundle().initialize(bootstrap)
    val validator: Validator = bootstrap.getValidatorFactory.getValidator

    "permit equal" in {
      validator.validate(Fixture(minDecimalBigInt = BigInt(5))).asScala must beEmpty
    }

    "permit larger" in {
      validator.validate(Fixture(minDecimalBigInt = BigInt(500))).asScala must beEmpty
    }

    "not permit smaller" in {
      validator.validate(Fixture(minDecimalBigInt = BigInt(4))).asScala.size must_== 1
    }
  }

  "@DecimalMax BigInt" should {

    val bootstrap = new Bootstrap[Configuration](mock[Application[Configuration]])

    new ScalaValidatorsBundle().initialize(bootstrap)
    val validator: Validator = bootstrap.getValidatorFactory.getValidator

    "permit equal" in {
      validator.validate(Fixture(maxDecimalBigInt = BigInt(15))).asScala must beEmpty
    }

    "permit smaller" in {
      validator.validate(Fixture(maxDecimalBigInt = BigInt(5))).asScala must beEmpty
    }

    "not permit larger" in {
      validator.validate(Fixture(maxDecimalBigInt = BigInt(500))).asScala.size must_== 1
    }
  }
}
