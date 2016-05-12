package com.datasift.dropwizard.scala.jdbi.tweak

import java.sql.{Types, PreparedStatement}

import org.scalatest.FlatSpec
import org.scalatest.mock.MockitoSugar
import org.skife.jdbi.v2.StatementContext
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => equalTo}

class BigDecimalArgumentFactorySpec extends FlatSpec with MockitoSugar {

  val factory = new BigDecimalArgumentFactory
  val ctx = mock[StatementContext]

  "BigDecimalArgumentFactory" should
    "accept scala.math.BigDecimal arguments" in {

    assert(factory.accepts(classOf[BigDecimal], BigDecimal(123.456), ctx))
  }

  it should "reject java.math.BigDecimal arguments" in {
    assert(!factory.accepts(
      classOf[java.math.BigDecimal], new java.math.BigDecimal(123.456), ctx))
  }

  it should "reject scala.math.BigInt" in {
    assert(!factory.accepts(classOf[BigInt], BigInt(123), ctx))
  }

  it should "bind an argument for scala.math.BigDecimal" in {
    val stmt = mock[PreparedStatement]
    val arg = factory.build(classOf[BigDecimal], BigDecimal(123.456), ctx)
    arg.apply(1, stmt, ctx)

    verify(stmt).setBigDecimal(
      equalTo(1),
      equalTo(BigDecimal(123.456).bigDecimal))
    verifyNoMoreInteractions(stmt)
  }

  it should "bind null when null argument" in {
    val stmt = mock[PreparedStatement]
    val arg = factory.build(classOf[BigDecimal], null, ctx)
    arg.apply(1, stmt, ctx)

    verify(stmt).setNull(equalTo(1), equalTo(Types.NUMERIC))
    verifyNoMoreInteractions(stmt)
  }

}
