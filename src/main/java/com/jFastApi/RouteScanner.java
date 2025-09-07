package com.jFastApi;

import com.jFastApi.annotation.HttpRoute;
import com.jFastApi.http.Response;
import com.jFastApi.util.ReflectionUtility;
import com.jFastApi.util.ResponseUtility;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class RouteScanner {

    public static void scanAndRegister(HttpServer server, String basePackage) {
        List<Method> methods = ReflectionUtility.findAnnotatedMethods(basePackage, HttpRoute.class);

        for (Method method : methods) {
            if (method.isAnnotationPresent(HttpRoute.class)) {
                HttpRoute route = method.getAnnotation(HttpRoute.class);

                // Must be instance method
                if (Modifier.isStatic(method.getModifiers())) {
                    throw new IllegalArgumentException("Route handler " + method + " must not be static.");
                }

                // Ensure method matches expected signature
                if (method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(HttpExchange.class)) {
                    server.createContext(route.path()).setHandler(exchange -> {
                        try {

                            // create a single instance of the controller
                            Object controllerInstance = method.getDeclaringClass()
                                    .getDeclaredConstructor()
                                    .newInstance();

                            Object result = method.invoke(controllerInstance, exchange); // invoke static method
                            sendResponse(result, exchange);

                        } catch (Exception e) {
                            exchange.sendResponseHeaders(500, 0);
                            exchange.close();
                            throw new RuntimeException("Failed to invoke handler", e);
                        }
                    });
                } else {
                    throw new IllegalArgumentException("Method " + method + " must take HttpExchange as only parameter.");
                }
            }
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
}
