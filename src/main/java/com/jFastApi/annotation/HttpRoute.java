package com.jFastApi.annotation;

import com.jFastApi.enumeration.HttpMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.TimeUnit;

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

    boolean authorized() default false;

    int limit() default 5;

    TimeUnit timeUnit() default TimeUnit.SECONDS;

    int time() default 1;

    boolean disableRateLimiter() default false;
}

