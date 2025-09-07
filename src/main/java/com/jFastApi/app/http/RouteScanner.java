package com.jFastApi.app.http;

import com.jFastApi.app.BeanFactory;
import com.jFastApi.app.annotation.HttpRoute;
import com.jFastApi.app.exception.ApplicationException;
import com.jFastApi.app.http.enumeration.HttpMethod;
import com.jFastApi.app.http.enumeration.HttpStatus;
import com.jFastApi.app.util.ReflectionUtility;
import com.jFastApi.app.util.ResponseUtility;
import com.sun.net.httpserver.HttpServer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class RouteScanner {

    public static void scanAndRegister(String basePackage) {
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

            RouteRegistry.register(new Route(
                    route.path(),
                    route.method(),
                    method,
                    method.getDeclaringClass()
            ));
        }
    }

    public static void registerDispatcher(HttpServer server) {
        server.createContext("/", exchange -> {
            try {
                String path = exchange.getRequestURI().getPath();
                HttpMethod httpMethod = HttpMethod.fromString(exchange.getRequestMethod());

                Route def = RouteRegistry.find(path, httpMethod);

                if (def == null) {
                    if (RouteRegistry.hasPath(path)) {

                        // Path exists but wrong method
                        ResponseUtility.sendErrorResponse(
                                new ApplicationException("Method Not Allowed"),
                                exchange,
                                HttpStatus.METHOD_NOT_ALLOWED
                        );
                    } else {

                        // Path does not exist in our registry
                        ResponseUtility.sendErrorResponse(
                                new ApplicationException("Not Found"),
                                exchange,
                                HttpStatus.NOT_FOUND
                        );
                    }
                    return;
                }

                Object controllerClass = BeanFactory.getBean(def.controllerClass());

                // Resolve parameters (your same parameter binding logic goes here)
                Object[] args = ParameterResolver.resolve(exchange, def.handlerMethod());

                Object result = def.handlerMethod().invoke(controllerClass, args);

                ResponseUtility.sendResponse(result, exchange);

            } catch (ApplicationException ex) {
                ResponseUtility.sendErrorResponse(ex, exchange, HttpStatus.BAD_REQUEST);
            } catch (Exception e) {
                ResponseUtility.sendErrorResponse(new ApplicationException("Internal Server Error"), exchange, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        });
    }
}
