package com.jFastApi.app;

import com.jFastApi.app.annotation.HttpRoute;
import com.jFastApi.app.annotation.RequestBody;
import com.jFastApi.app.annotation.RequestParam;
import com.jFastApi.app.exception.ApplicationException;
import com.jFastApi.app.http.enumeration.HttpMethod;
import com.jFastApi.app.util.JsonUtility;
import com.jFastApi.app.util.QueryParamUtil;
import com.jFastApi.app.util.ReflectionUtility;
import com.jFastApi.app.util.ResponseUtility;
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

            HttpRoute route = method.getAnnotation(HttpRoute.class); // Get Annotation instance with value

            // Must be instance method
            if (Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("Route handler " + method + " must not be static.");
            }

            server.createContext(route.path()).setHandler(exchange -> {
                try {

                    HttpMethod httpMethod = HttpMethod.fromString(exchange.getRequestMethod());
                    if (!route.method().equals(httpMethod)) {
                        throw new ApplicationException("Invalid Http Method!");
                    }

                    Object controllerInstance = BeanFactory.getBean(method.getDeclaringClass());

                    // Resolve method parameters
                    int parameterCount = method.getParameterCount();
                    Object[] args = new Object[parameterCount];
                    for (int i = 0; i < parameterCount; i++) {
                        Parameter param = method.getParameters()[i];

                        if (param.isAnnotationPresent(RequestBody.class) && httpMethod.equals(HttpMethod.POST)) {

                            // Deserialize body JSON
                            args[i] = JsonUtility.fromJson(exchange.getRequestBody(), param.getType());

                        } else if (param.isAnnotationPresent(RequestParam.class)) {

                            // Get Annotation instance with value
                            RequestParam rp = param.getAnnotation(RequestParam.class);
                            Map<String, String> query = QueryParamUtil.parseQuery(exchange.getRequestURI().getRawQuery());

                            String value = query.get(rp.name());
                            if (value == null) {
                                if (rp.required() && rp.defaultValue().isEmpty()) {
                                    throw new ApplicationException("Missing required query param: " + rp.name());
                                }
                                value = rp.defaultValue();
                            }
                            args[i] = convertType(value, param.getType()); // simple type conversion

                        } else {
                            throw new ApplicationException("Unsupported parameter binding for: " + param);
                        }
                    }

                    // Invoke method
                    Object result = method.invoke(controllerInstance, args);

                    ResponseUtility.sendResponse(result, exchange);

                } catch (ApplicationException ex) {
                    ResponseUtility.sendErrorResponse(ex, exchange);
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

    private static Object convertType(String value, Class<?> type) {
        if (type.equals(String.class)) return value;
        if (type.equals(int.class) || type.equals(Integer.class)) return Integer.parseInt(value);
        if (type.equals(long.class) || type.equals(Long.class)) return Long.parseLong(value);
        if (type.equals(boolean.class) || type.equals(Boolean.class)) return Boolean.parseBoolean(value);
        // add more as needed
        throw new IllegalArgumentException("Unsupported param type: " + type);
    }
}
