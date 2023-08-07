package net.dbinfun.ioc.annotation;

import net.dbinfun.ioc.beans.BeanType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {
    String value() default "";// service name
    BeanType type() default BeanType.original;
}
