package net.dbinfun.ioc.annotation;

import net.dbinfun.ioc.beans.RequestType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
public @interface Request {
    String[] path() default {"/"};// service name
    RequestType requestType() default RequestType.GET;
}
