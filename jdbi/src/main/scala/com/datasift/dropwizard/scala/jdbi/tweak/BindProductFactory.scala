package com.datasift.dropwizard.scala.jdbi.tweak

import java.lang.annotation.Annotation

import com.datasift.dropwizard.jdbi.tweak.BindProduct
import org.skife.jdbi.v2.SQLStatement
import org.skife.jdbi.v2.sqlobject.{Binder, BinderFactory}

class BindProductFactory extends BinderFactory {
  override def build(annotation: Annotation): Binder[_ <: Annotation, _] = {
    new Binder[BindProduct, Product] {
      override def bind(q: SQLStatement[_],
                        bind: BindProduct,
                        arg: Product): Unit = {
        val prefix = if (bind.value == "__jdbi_bare__") "" else bind.value + "."
        val fields = arg.getClass.getDeclaredFields
        val methods = arg.getClass.getDeclaredMethods.collect {
          case m if m.getParameterCount == 0 => m.getName -> m
        }.toMap

        for {
          field <- fields
        } {
          val name = field.getName
          val value = methods.get(name)
            .map(_.invoke(arg))
            .getOrElse(field.get(arg)) match {
            case None => null
            case Some(x) => x
            case x => x
          }

          q.bind(prefix + name, value)
        }
      }
    }
  }
}
