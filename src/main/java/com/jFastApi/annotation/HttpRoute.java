package com.jFastApi.annotation;

import com.jFastApi.enumeration.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HttpRoute {
    String path();

    HttpMethod method() default HttpMethod.GET;

    /**
     * Roles allowed to access this route.
     * Empty array means public route.
     */
    String[] roles() default {};
}

