package com.jFastApi.app.annotation;

import com.jFastApi.app.http.enumeration.HttpMethod;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface HttpRoute {
    String path();
    HttpMethod method() default HttpMethod.GET;
}

