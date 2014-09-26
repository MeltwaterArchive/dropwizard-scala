package com.datasift.dropwizard.scala.validation

import scala.annotation.meta.{param, field}
import javax.validation.{constraints => jsr}
import org.hibernate.validator.{constraints => hibernate}

/** Type aliased constraints for case classes. */
package object constraints {

  // JSR-303 constraints
  type AssertFalse = jsr.AssertFalse @field @param
  type AssertTrue = jsr.AssertTrue @field @param
  type DecimalMax = jsr.DecimalMax @field @param
  type DecimalMin = jsr.DecimalMin @field @param
  type Digits = jsr.Digits @field @param
  type Future = jsr.Future @field @param
  type Max = jsr.Max @field @param
  type Min = jsr.Min @field @param
  type NotNull = jsr.NotNull @field @param
  type Null = jsr.Null @field @param
  type Past = jsr.Past @field @param
  type Pattern = jsr.Pattern @field @param
  type Size = jsr.Size @field @param
  type Valid = javax.validation.Valid @field @param

  // extra Hibernate Validator constraints
  type ConstraintComposition = hibernate.ConstraintComposition
  type CreditCardNumber = hibernate.CreditCardNumber @field @param
  type Email = hibernate.Email @field @param
  type Length = hibernate.Length @field @param
  type ModCheck = hibernate.ModCheck @field @param
  type NotBlank = hibernate.NotBlank @field @param
  type NotEmpty = hibernate.NotEmpty @field @param
  type Range = hibernate.Range @field @param
  type SafeHtml = hibernate.SafeHtml @field @param
  type ScriptAssert = hibernate.ScriptAssert
  type URL = hibernate.URL @field @param
}
