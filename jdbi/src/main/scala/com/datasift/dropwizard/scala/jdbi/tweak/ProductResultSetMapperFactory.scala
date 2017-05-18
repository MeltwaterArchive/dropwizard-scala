package com.datasift.dropwizard.scala.jdbi.tweak

import java.lang.reflect.{InvocationTargetException, Constructor}
import java.sql.{Time, Timestamp, ResultSet}
import java.util.Date

import org.skife.jdbi.v2.tweak.ResultSetMapper
import org.skife.jdbi.v2.{StatementContext, ResultSetMapperFactory}

class ProductResultSetMapperFactory extends ResultSetMapperFactory {

  override def accepts(tpe: Class[_],
                       ctx: StatementContext): Boolean =
    classOf[Product].isAssignableFrom(tpe)

  override def mapperFor(tpe: Class[_],
                         ctx: StatementContext): ResultSetMapper[_] = {
    new ProductResultSetMapper(tpe.asInstanceOf[Class[_ <: Product]])
  }
}

class ProductResultSetMapper[A <: Product](tpe: Class[A])
  extends ResultSetMapper[A] {

  // catalogue available constructors, by parameter names
  private[this] val ctor = tpe.getConstructors.head.asInstanceOf[Constructor[A]]

  override def map(index: Int,
                   rs: ResultSet,
                   ctx: StatementContext): A = {
    // use constructor parameter types to determine type to marshall column
    // from
    val types = ctor.getParameterTypes
    val values = for {
      (t, idx) <- types.zipWithIndex
    } yield {
      val i = idx + 1
      t match {
        // todo: do we need to explicitly match on the java variations of these types too?
        case _ if t.isAssignableFrom(classOf[Option[_]]) =>
          Option(rs.getObject(i))
        case _ if !t.isPrimitive && (rs.getObject(i) == null) =>
          null
        case _ if t.isAssignableFrom(classOf[Boolean]) =>
          new java.lang.Boolean(rs.getBoolean(i))
        case _ if t.isAssignableFrom(classOf[Byte]) =>
          new java.lang.Byte(rs.getByte(i))
        case _ if t.isAssignableFrom(classOf[Short]) =>
          new java.lang.Short(rs.getShort(i))
        case _ if t.isAssignableFrom(classOf[Int]) =>
          new Integer(rs.getInt(i))
        case _ if t.isAssignableFrom(classOf[Long]) =>
          new java.lang.Long(rs.getLong(i))
        case _ if t.isAssignableFrom(classOf[Double]) =>
          new java.lang.Double(rs.getDouble(i))
        case _ if t.isAssignableFrom(classOf[Float]) =>
          new java.lang.Float(rs.getFloat(i))
        case _ if t.isAssignableFrom(classOf[java.math.BigDecimal]) =>
          rs.getBigDecimal(i)
        case _ if t.isAssignableFrom(classOf[BigDecimal]) =>
          BigDecimal(rs.getBigDecimal(i))
        case _ if t.isAssignableFrom(classOf[Timestamp]) =>
          rs.getTimestamp(i)
        case _ if t.isAssignableFrom(classOf[Time]) =>
          rs.getTime(i)
        case _ if t.isAssignableFrom(classOf[Date]) =>
          rs.getDate(i)
        case _ if t.isAssignableFrom(classOf[String]) =>
          rs.getString(i)
        case _ if t.isEnum =>
          Option(rs.getString(i))
            .map(x => t.getEnumConstants.find(_.asInstanceOf[Enum[_]].name() == x))
            .orNull
        case _ => rs.getObject(i)
      }
    }

    try {
      ctor.newInstance(values: _*)
    } catch {
      case e: IllegalAccessException => throw new IllegalArgumentException(
        s"Constructor for ${tpe.getSimpleName} inaccessible", e)
      case e: InvocationTargetException =>
        throw new IllegalArgumentException("Constructor threw Exception", e)
      case e: InstantiationException => throw new IllegalArgumentException(
        s"Cannot create instances of abstract class: ${tpe.getSimpleName}",
        e)
      case e: ExceptionInInitializerError =>
        throw new IllegalArgumentException(
          s"Failed to initialize object of type: ${tpe.getSimpleName}", e)
    }
  }
}
