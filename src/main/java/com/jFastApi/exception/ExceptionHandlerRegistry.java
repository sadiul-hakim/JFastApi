package com.jFastApi.exception;

import com.jFastApi.BeanFactory;
import com.jFastApi.annotation.ExceptionHandler;
import com.jFastApi.util.ReflectionUtility;
import com.jFastApi.util.ResponseUtility;
import com.sun.net.httpserver.HttpExchange;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing global exception handlers.
 * <p>
 * - Allows registering methods annotated with @ExceptionHandler
 * to handle specific exception types.
 * - Provides a default handler if no matching handler is found.
 * - Thread-safe via ConcurrentHashMap for handler storage.
 */
public final class ExceptionHandlerRegistry {

    /**
     * Stores exception type -> handler mapping.
     * ConcurrentHashMap ensures thread-safety for handler lookups.
     */
    private static final Map<Class<? extends Throwable>, ExceptionHandlerInvoker> handlers = new ConcurrentHashMap<>();

    /**
     * Default handler used when no specific handler is registered
     * for a given exception type.
     */
    private static ExceptionHandlerInvoker defaultHandler = new DefaultGlobalExceptionHandler();

    /**
     * Scans for methods annotated with @ExceptionHandler in the given base package
     * and registers them as handlers.
     *
     * @param basePackage the package to scan for annotated methods
     */
    public static void scanPackage(String basePackage, String internalBasePackage) {
        // Find all methods annotated with @ExceptionHandler
        List<Method> methods = ReflectionUtility.findAnnotatedMethods(basePackage, ExceptionHandler.class);
        List<Method> internalMethods = ReflectionUtility.findAnnotatedMethods(internalBasePackage, ExceptionHandler.class);

        register(methods);
        register(internalMethods);
    }

    private static void register(List<Method> methods) {
        for (Method method : methods) {
            // Double-check annotation presence (defensive programming)
            if (!method.isAnnotationPresent(ExceptionHandler.class)) {
                continue;
            }

            ExceptionHandler ann = method.getAnnotation(ExceptionHandler.class);

            try {
                // Obtain an instance of the class containing the handler method
                Object instance = BeanFactory.getBeanInstance(method.getDeclaringClass());

                // Register the method for each exception type declared in the annotation
                for (Class<? extends Throwable> exType : ann.exception()) {
                    registerMethodHandler(exType, instance, method);
                }
            } catch (Exception ignored) {
                // Could log this instead of ignoring (useful for debugging)
            }
        }
    }

    /**
     * Registers a handler method for a specific exception type.
     *
     * @param exType   the exception type to handle
     * @param instance the instance containing the handler method
     * @param method   the handler method
     */
    private static void registerMethodHandler(Class<? extends Throwable> exType, Object instance, Method method) {
        method.setAccessible(true); // allow access to private/protected methods

        handlers.put(exType, (ex, exchange) -> {
            try {
                // Invoke the handler method with the exception
                Object result = method.invoke(instance, ex);

                // Send the handler's result as HTTP response
                ResponseUtility.sendResponse(result, exchange);
            } catch (InvocationTargetException ite) {
                // If the invoked method itself throws, unwrap the cause
                Throwable cause = ite.getTargetException();

                if (cause instanceof Exception) {
                    throw (Exception) cause; // rethrow checked exception
                } else {
                    throw new RuntimeException(cause); // wrap unchecked
                }
            }
        });
    }

    /**
     * Handles the given exception using a registered handler
     * or falls back to the default handler if none is found.
     *
     * @param ex       the exception to handle
     * @param exchange the current HTTP exchange
     * @throws Exception if the handler itself throws
     */
    public static void handle(Throwable ex, HttpExchange exchange) throws Exception {
        ExceptionHandlerInvoker handler = findHandler(ex.getClass());
        handler.handle(ex, exchange);
    }

    /**
     * Finds a handler for the given exception type.
     * - Checks for exact match first.
     * - Falls back to checking assignable types (subclass handling).
     * - Returns the default handler if none found.
     *
     * @param exType the exception class
     * @return the resolved handler
     */
    private static ExceptionHandlerInvoker findHandler(Class<?> exType) {
        // Direct type match
        if (handlers.containsKey(exType)) return handlers.get(exType);

        // Check for superclass/assignable matches
        for (Map.Entry<Class<? extends Throwable>, ExceptionHandlerInvoker> entry : handlers.entrySet()) {
            if (entry.getKey().isAssignableFrom(exType)) {
                return entry.getValue();
            }
        }

        // Fallback to default
        return defaultHandler;
    }

    /**
     * Overrides the default handler with a custom one.
     *
     * @param handler the new default handler
     */
    public static void setDefaultHandler(ExceptionHandlerInvoker handler) {
        defaultHandler = handler;
    }
}

