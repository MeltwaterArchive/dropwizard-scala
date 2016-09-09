
package com.datasift.dropwizard.scala.jdbi

import java.sql.ResultSet

import org.skife.jdbi.v2.StatementContext
import org.skife.jdbi.v2.tweak.ResultSetMapper


/**
  * A simple mapper that only depends on the result set
  *
  * Example -
  *   SimpleMapper[(String, String)](r => (r.getString("firstname"), r.getString("lastname")))
  *
  * If you used with [[ResultSetDSL]] the above example can be further simplified -
  *   SimpleMapper[(String, String)](r => (r -> "firstname", r -> "lastname"))
  */
class SimpleMapper[A](f: ResultSet => A) extends ResultSetMapper[A] {
  def map(idx: Int, rs: ResultSet, ctx: StatementContext): A = f(rs)
}

