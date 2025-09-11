package com.jFastApi.http;

import com.jFastApi.enumeration.HttpMethod;

import java.lang.reflect.Method;
import java.util.List;

public record Route(String path, HttpMethod method, Method handlerMethod, Class<?> controllerClass,
                    List<String> authorities, boolean authorized) {
}

