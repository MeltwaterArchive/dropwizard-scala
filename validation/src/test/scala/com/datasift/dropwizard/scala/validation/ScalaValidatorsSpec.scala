package com.datasift.dropwizard.scala.validation

import org.scalatest.FlatSpec

import io.dropwizard.setup.{Environment, Bootstrap}
import io.dropwizard.{Configuration, Application}
import com.datasift.dropwizard.scala.validation.constraints._

import scala.collection.JavaConverters.asScalaSetConverter

import javax.validation.Validator

class ScalaValidatorsSpec extends FlatSpec {

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

  object MockApplication extends Application[Configuration] {
    def run(configuration: Configuration, environment: Environment) {}
    def initialize(bootstrap: Bootstrap[Configuration]) {}
  }

  val bootstrap = new Bootstrap[Configuration](MockApplication)

  new ScalaValidatorsBundle().initialize(bootstrap)
  val validator: Validator = bootstrap.getValidatorFactory.getValidator

  "@NotEmpty Option" should "not permit None" in {
    val violations = validator.validate(Fixture(mandatoryOption = None)).asScala
    assert(violations.size === 1)
  }

  it should "permit Some" in {
    assert(validator.validate(Fixture(mandatoryOption = Option(5))).asScala.isEmpty)
  }


  "@NotEmpty List" should "not permit empty List" in {
    val violations = validator.validate(Fixture(mandatoryList = Nil)).asScala
    assert(violations.size === 1)
  }

  it should "permit List with elements" in {
    assert(validator.validate(Fixture(mandatoryList = List(5))).asScala.isEmpty)
  }


  "@Size List" should "permit in-range" in {
    assert(validator.validate(Fixture(shortList = List(1, 2))).asScala.isEmpty)
  }

  it should "not permit too few" in {
    val violations = validator.validate(Fixture(shortList = Nil)).asScala
    assert(violations.size === 1)
  }

  it should "not permit too many" in {
    val violations = validator.validate(Fixture(shortList = List(1, 2, 3, 4, 5))).asScala
    assert(violations.size === 1)
  }


  "@AssertTrue Boolean" should "permit true" in {
    assert(validator.validate(Fixture(beTrue = true)).asScala.isEmpty)
  }

  it should "not permit false" in {
    assert(validator.validate(Fixture(beTrue = false)).asScala.size === 1)
  }


  "@AssertFalse Boolean" should "permit false" in {
    assert(validator.validate(Fixture(beFalse = false)).asScala.isEmpty)
  }

  it should "not permit true" in {
    assert(validator.validate(Fixture(beFalse = true)).asScala.size === 1)
  }


  "@Min Int" should "permit equal" in {
    assert(validator.validate(Fixture(minInt = 5)).asScala.isEmpty)
  }

  it should "permit larger" in {
    assert(validator.validate(Fixture(minInt = 500)).asScala.isEmpty)
  }

  it should "not permit smaller" in {
    assert(validator.validate(Fixture(minInt = 4)).asScala.size === 1)
  }


  "@Max Int" should "permit equal" in {
    assert(validator.validate(Fixture(maxInt = 15)).asScala.isEmpty)
  }

  it should "permit smaller" in {
    assert(validator.validate(Fixture(maxInt = 5)).asScala.isEmpty)
  }

  it should "not permit larger" in {
    assert(validator.validate(Fixture(maxInt = 500)).asScala.size === 1)
  }


  "@Min BigInt" should "permit equal" in {
    assert(validator.validate(Fixture(minBigInt = BigInt(5))).asScala.isEmpty)
  }

  it should "permit larger" in {
    assert(validator.validate(Fixture(minBigInt = BigInt(500))).asScala.isEmpty)
  }

  it should "not permit smaller" in {
    assert(validator.validate(Fixture(minBigInt = BigInt(4))).asScala.size === 1)
  }


  "@Max BigInt" should "permit equal" in {
    assert(validator.validate(Fixture(maxBigInt = BigInt(15))).asScala.isEmpty)
  }

  it should "permit smaller" in {
    assert(validator.validate(Fixture(maxBigInt = BigInt(5))).asScala.isEmpty)
  }

  it should "not permit larger" in {
    assert(validator.validate(Fixture(maxBigInt = BigInt(500))).asScala.size === 1)
  }


  "@DecimalMin BigInt" should "permit equal" in {
    assert(validator.validate(Fixture(minDecimalBigInt = BigInt(5))).asScala.isEmpty)
  }

  it should "permit larger" in {
    assert(validator.validate(Fixture(minDecimalBigInt = BigInt(500))).asScala.isEmpty)
  }

  it should "not permit smaller" in {
    assert(validator.validate(Fixture(minDecimalBigInt = BigInt(4))).asScala.size === 1)
  }


  "@DecimalMax BigInt" should "permit equal" in {
    assert(validator.validate(Fixture(maxDecimalBigInt = BigInt(15))).asScala.isEmpty)
  }

  it should "permit smaller" in {
    assert(validator.validate(Fixture(maxDecimalBigInt = BigInt(5))).asScala.isEmpty)
  }

  it should "not permit larger" in {
    assert(validator.validate(Fixture(maxDecimalBigInt = BigInt(500))).asScala.size === 1)
  }
}
