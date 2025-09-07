package com.jFastApi.app.http;

import com.jFastApi.app.http.enumeration.HttpMethod;

import java.lang.reflect.Method;

public record Route(String path, HttpMethod method, Method handlerMethod, Class<?> controllerClass) {
}

