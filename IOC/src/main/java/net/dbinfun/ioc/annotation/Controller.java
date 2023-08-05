package net.dbinfun.ioc.annotation;

import net.dbinfun.ioc.beans.BeanType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Controller {
    String value() default "";// service name
    BeanType type() default BeanType.original;
}
