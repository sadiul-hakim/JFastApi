package com.jFastApi.annotation;

import com.jFastApi.http.enumeration.HttpMethod;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HttpRoute {
    String path();
    HttpMethod method() default HttpMethod.GET;
}

