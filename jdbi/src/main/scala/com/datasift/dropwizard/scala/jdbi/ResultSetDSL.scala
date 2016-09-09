
package com.datasift.dropwizard.scala.jdbi

import java.sql.{ResultSet, Timestamp}
import java.util.Date


/** Define some implicits to allow a more concise DSL using Tuples for extracting results from an SQL result set. */
object ResultSetDSL {

  implicit def getString(krs: (ResultSet, String)): String =
    krs._1.getString(krs._2)

  implicit def getInt(krs: (ResultSet, String)): Int =
    krs._1.getInt(krs._2)

  implicit def getLong(krs: (ResultSet, String)): Long =
    krs._1.getLong(krs._2)

  implicit def getBoolean(krs: (ResultSet, String)): Boolean =
    krs._1.getBoolean(krs._2)

  implicit def getBytes(krs: (ResultSet, String)): Array[Byte] =
    krs._1.getBytes(krs._2)

  implicit def getDate(krs: (ResultSet, String)): Date =
    krs._1.getDate(krs._2)

  implicit def getTimestamp(krs: (ResultSet, String)): Timestamp =
    krs._1.getTimestamp(krs._2)
}

