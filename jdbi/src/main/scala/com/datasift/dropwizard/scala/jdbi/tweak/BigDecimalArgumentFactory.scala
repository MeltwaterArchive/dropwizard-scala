package com.datasift.dropwizard.scala.jdbi.tweak

import java.sql.{PreparedStatement, Types}

import org.skife.jdbi.v2.tweak.{ArgumentFactory, Argument}
import org.skife.jdbi.v2.StatementContext

class BigDecimalArgumentFactory(driver: String) extends ArgumentFactory[BigDecimal] {

  def accepts(expectedType: Class[_], value: Any, ctx: StatementContext): Boolean = {
    value.isInstanceOf[BigDecimal]
  }

  def build(expectedType: Class[_], value: BigDecimal, ctx: StatementContext): Argument = {
    driver match {
      case "com.microsoft.sqlserver.jdbc.SQLServerDriver" => new Argument {
        def apply(position: Int, statement: PreparedStatement, ctx: StatementContext) {
          if (value != null) statement.setObject(position, value.bigDecimal)
          else statement.setObject(position, value)
        }
      }
      case _ => new Argument {
        def apply(position: Int, statement: PreparedStatement, ctx: StatementContext) {
          if (value != null) statement.setObject(position, value.bigDecimal)
          else statement.setNull(position, Types.DECIMAL)
        }
      }
    }
  }
}


