package com.datasift.dropwizard.jdbi.tweak;

import com.datasift.dropwizard.scala.jdbi.tweak.BindProductFactory;
import org.skife.jdbi.v2.sqlobject.BindingAnnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@BindingAnnotation(BindProductFactory.class)
public @interface BindProduct {
    String value() default "__jdbi_bare__";
}
