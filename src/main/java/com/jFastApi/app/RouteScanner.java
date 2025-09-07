package com.jFastApi.app;

import com.jFastApi.annotation.HttpRoute;
import com.jFastApi.annotation.RequestBody;
import com.jFastApi.annotation.RequestParam;
import com.jFastApi.http.Response;
import com.jFastApi.util.JsonUtility;
import com.jFastApi.util.QueryParamUtil;
import com.jFastApi.util.ReflectionUtility;
import com.jFastApi.util.ResponseUtility;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

public class RouteScanner {

    public static void scanAndRegister(HttpServer server, String basePackage) {
        List<Method> methods = ReflectionUtility.findAnnotatedMethods(basePackage, HttpRoute.class);

        for (Method method : methods) {
            if (!method.isAnnotationPresent(HttpRoute.class)) {
                continue;
            }

            HttpRoute route = method.getAnnotation(HttpRoute.class);

            // Must be instance method
            if (Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("Route handler " + method + " must not be static.");
            }

            server.createContext(route.path()).setHandler(exchange -> {
                try {
                    Object controllerInstance = BeanFactory.getBean(method.getDeclaringClass());

                    // Resolve method parameters
                    Object[] args = new Object[method.getParameterCount()];
                    for (int i = 0; i < method.getParameterCount(); i++) {
                        Parameter param = method.getParameters()[i];

                        if (param.isAnnotationPresent(RequestBody.class) && exchange.getRequestMethod().equals("POST")) {

                            // Deserialize body JSON
                            args[i] = JsonUtility.fromJson(exchange.getRequestBody(), param.getType());

                        } else if (param.isAnnotationPresent(RequestParam.class)) {
                            RequestParam rp = param.getAnnotation(RequestParam.class);
                            Map<String, String> query = QueryParamUtil.parseQuery(exchange.getRequestURI().getRawQuery());

                            String value = query.get(rp.name());
                            if (value == null) {
                                if (rp.required() && rp.defaultValue().isEmpty()) {
                                    throw new IllegalArgumentException("Missing required query param: " + rp.name());
                                }
                                value = rp.defaultValue();
                            }
                            args[i] = convertType(value, param.getType()); // simple type conversion

                        } else {
                            throw new IllegalArgumentException("Unsupported parameter binding for: " + param);
                        }
                    }

                    // Invoke method
                    Object result = method.invoke(controllerInstance, args);

                    sendResponse(result, exchange);

                } catch (Exception e) {
                    try {
                        exchange.sendResponseHeaders(500, 0);
                    } catch (IOException ignored) {
                    }
                    exchange.close();
                    throw new RuntimeException("Failed to invoke handler: " + method, e);
                }
            });
        }
    }

    private static void sendResponse(Object result, HttpExchange exchange) {

        try {

            if (result instanceof Response<?> response) {
                ResponseUtility.sendResponse(exchange, response);
            } else {

                // optional: allow void methods too
                exchange.sendResponseHeaders(204, -1); // No Content
                exchange.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Object convertType(String value, Class<?> type) {
        if (type.equals(String.class)) return value;
        if (type.equals(int.class) || type.equals(Integer.class)) return Integer.parseInt(value);
        if (type.equals(long.class) || type.equals(Long.class)) return Long.parseLong(value);
        if (type.equals(boolean.class) || type.equals(Boolean.class)) return Boolean.parseBoolean(value);
        // add more as needed
        throw new IllegalArgumentException("Unsupported param type: " + type);
    }
}
