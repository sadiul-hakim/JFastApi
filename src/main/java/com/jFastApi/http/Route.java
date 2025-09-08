package com.jFastApi.http;

import com.jFastApi.http.enumeration.HttpMethod;

import java.lang.reflect.Method;

public record Route(String path, HttpMethod method, Method handlerMethod, Class<?> controllerClass) {
}

