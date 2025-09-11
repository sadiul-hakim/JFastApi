package com.jFastApi.http;

import com.jFastApi.BeanFactory;
import com.jFastApi.annotation.HttpRoute;
import com.jFastApi.enumeration.HttpMethod;
import com.jFastApi.enumeration.HttpStatus;
import com.jFastApi.exception.*;
import com.jFastApi.http.interceptor.Interceptor;
import com.jFastApi.http.interceptor.InterceptorRegistry;
import com.jFastApi.security.AuthenticationException;
import com.jFastApi.util.ReflectionUtility;
import com.jFastApi.util.ResponseUtility;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import io.jsonwebtoken.ExpiredJwtException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * RouteScanner is responsible for discovering and registering HTTP route handlers.
 * <p>
 * Responsibilities:
 * 1. Scans a given base package for methods annotated with @HttpRoute.
 * 2. Registers discovered routes into the RouteRegistry.
 * 3. Creates a dispatcher (via HttpServer) that delegates incoming requests
 * to the correct registered route handler.
 */
public class RouteScanner {

    /**
     * Scans the given base package for methods annotated with @HttpRoute
     * and registers them in the RouteRegistry.
     *
     * @param basePackage The root package to scan for route handler methods.
     */
    public static void scanAndRegister(String basePackage, String internalBasePackage) {
        // Find all methods annotated with @HttpRoute in the package
        List<Method> methods = ReflectionUtility.findAnnotatedMethods(basePackage, HttpRoute.class);
        List<Method> internalMethod = ReflectionUtility.findAnnotatedMethods(internalBasePackage, HttpRoute.class);

        registerRoutes(methods);
        registerRoutes(internalMethod);
    }

    private static void registerRoutes(List<Method> methods) {

        for (Method method : methods) {

            // Double-check method is actually annotated (safety check)
            if (!method.isAnnotationPresent(HttpRoute.class)) {
                continue;
            }

            // Extract @HttpRoute annotation to get path + method info
            HttpRoute route = method.getAnnotation(HttpRoute.class);

            // Route handler must not be static (must belong to a controller instance)
            if (Modifier.isStatic(method.getModifiers())) {
                throw new ApplicationException("Route handler " + method + " must not be static.");
            }

            // Register discovered route into central registry
            RouteRegistry.register(new Route(
                    route.path(),              // Path (e.g., "/users")
                    route.method(),            // HTTP method (GET, POST, etc.)
                    method,                    // Handler method reference
                    method.getDeclaringClass(), // Controller class that owns the method
                    Arrays.asList(route.roles()),
                    route.roles().length > 0 || route.authorized(),
                    route.limit(),
                    route.timeUnit(),
                    route.time(),
                    route.disableRateLimiter()
            ));
        }
    }

    /**
     * Registers the main request dispatcher into the given HttpServer.
     * <p>
     * The dispatcher:
     * - Matches incoming requests (path + method) against registered routes.
     * - Invokes the appropriate controller method if found.
     * - Returns appropriate error responses if no route matches.
     *
     * @param server The HttpServer instance to attach the dispatcher to.
     */
    public static void registerDispatcher(HttpServer server) {

        // Attach a handler for all paths ("root" dispatcher)
        server.createContext("/", RouteScanner::handleRootEndpoint);
    }

    private static void handleRootEndpoint(HttpExchange exchange) {

        // Get registered interceptors
        Collection<Interceptor> interceptors = InterceptorRegistry.getInterceptors();

        Route route = null; // declare here so it's visible in catch blocks

        try {
            // Extract path and method from incoming request
            String path = exchange.getRequestURI().getPath();
            HttpMethod httpMethod = HttpMethod.fromString(exchange.getRequestMethod());

            // Look up matching route from registry
            route = RouteRegistry.find(path, httpMethod);

            if (route == null) {
                if (RouteRegistry.hasPath(path)) {
                    // Path exists but method is not allowed (405)
                    ResponseUtility.sendErrorResponse(
                            new ApplicationException("Method Not Allowed"),
                            exchange,
                            HttpStatus.METHOD_NOT_ALLOWED
                    );
                } else {
                    // Path does not exist at all (404)
                    ResponseUtility.sendErrorResponse(
                            new ApplicationException("Not Found"),
                            exchange,
                            HttpStatus.NOT_FOUND
                    );
                }
                return;
            }

            // Run preHandle
            for (Interceptor interceptor : interceptors) {
                if (!interceptor.preHandle(exchange, route)) {
                    return; // stop if interceptor blocks request
                }
            }

            // Get controller instance from BeanFactory (DI)
            Object controllerClass = BeanFactory.getBeanInstance(route.controllerClass());

            // Resolve parameters for handler method (query, body, headers, etc.)
            Object[] params = ParameterResolver.resolve(exchange, route.handlerMethod());

            // Invoke the controller method with resolved parameters
            Object result = route.handlerMethod().invoke(controllerClass, params);

            // Run postHandle
            for (Interceptor interceptor : interceptors) {
                result = interceptor.postHandle(exchange, route, result);
            }

            // Send the method's return value as HTTP response
            ResponseUtility.sendResponse(result, exchange);

        } catch (TooManyRequestException ex) {

            ResponseUtility.sendErrorResponse(ex, exchange, HttpStatus.FORBIDDEN);
        } catch (ApplicationException ex) {

            // Known application-level error → return 400
            ResponseUtility.sendErrorResponse(ex, exchange, HttpStatus.BAD_REQUEST);

        } catch (ForbiddenException ex) {

            // Known application-level error → return 400
            ResponseUtility.sendErrorResponse(ex, exchange, HttpStatus.FORBIDDEN);

        } catch (ExpiredJwtException ex) {

            ResponseUtility.sendErrorResponse(ex, exchange, HttpStatus.UNAUTHORIZED);
        } catch (UnauthorizedException | AuthenticationException ex) {

            // Known application-level error → return 400
            ResponseUtility.sendErrorResponse(ex, exchange, HttpStatus.UNAUTHORIZED);

        } catch (InvocationTargetException e) {

            boolean handled = false;
            Throwable targetException = e.getTargetException();
            for (Interceptor interceptor : interceptors) {
                if (interceptor.onException(exchange, route, targetException)) {
                    handled = true;
                    break;
                }
            }

            if (!handled) {
                try {
                    ExceptionHandlerRegistry.handle(targetException, exchange);
                } catch (Exception ex) {
                    ResponseUtility.sendErrorResponse(
                            new ApplicationException("Internal Server Error"),
                            exchange,
                            HttpStatus.INTERNAL_SERVER_ERROR
                    );
                }
            }
        } catch (Exception e) {
            ResponseUtility.sendErrorResponse(
                    new ApplicationException("Internal Server Error"),
                    exchange,
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }
}
