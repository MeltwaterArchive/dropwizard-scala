package com.datasift.dropwizard.scala.jdbi.tweak

import java.sql.{Types, PreparedStatement}

import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.{Argument, ArgumentFactory}

class BigDecimalArgumentFactory extends ArgumentFactory[BigDecimal] {

  override def accepts(expectedType: Class[_],
                       value: Any,
                       ctx: StatementContext): Boolean =
    value.isInstanceOf[BigDecimal]

  override def build(expectedType: Class[_],
                     value: BigDecimal,
                     ctx: StatementContext): Argument =
    new BigDecimalArgument(value)
}

class BigDecimalArgument(value: BigDecimal) extends Argument {

  override def apply(position: Int,
                     statement: PreparedStatement,
                     ctx: StatementContext): Unit = value match {
    case null => statement.setNull(position, Types.NUMERIC)
    case _ => statement.setBigDecimal(position, value.bigDecimal)
  }

  override def toString = String.valueOf(value)
}

