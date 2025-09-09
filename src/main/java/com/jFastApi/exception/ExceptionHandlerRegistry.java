package com.jFastApi.exception;

import com.jFastApi.BeanFactory;
import com.jFastApi.annotation.ExceptionHandler;
import com.jFastApi.util.ReflectionUtility;
import com.jFastApi.util.ResponseUtility;
import com.sun.net.httpserver.HttpExchange;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ExceptionHandlerRegistry {

    private static final Map<Class<? extends Throwable>, ExceptionHandlerInvoker> handlers = new HashMap<>();

    private static ExceptionHandlerInvoker defaultHandler;

    public ExceptionHandlerRegistry() {
        defaultHandler = new DefaultGlobalExceptionHandler();
    }

    /**
     * Scans for methods annotated with @ExceptionHandler in the given base package
     * and registers them.
     */
    public static void scanPackage(String basePackage) {

        List<Method> methods = ReflectionUtility.findAnnotatedMethods(basePackage, ExceptionHandler.class);
        for (Method method : methods) {
            if (method.isAnnotationPresent(ExceptionHandler.class)) {
                ExceptionHandler ann = method.getAnnotation(ExceptionHandler.class);
                try {
                    Object instance = BeanFactory.getBean(method.getDeclaringClass());
                    for (Class<? extends Exception> exType : ann.exception()) {
                        registerMethodHandler(exType, instance, method);
                    }
                } catch (Exception ignored) {
                    // could log this if desired
                }
            }
        }
    }

    private static void registerMethodHandler(Class<? extends Exception> exType, Object instance, Method method) {
        method.setAccessible(true);
        handlers.put(exType, (ex, exchange) -> {
            try {
                Object result = method.invoke(instance, ex);
                ResponseUtility.sendResponse(result, exchange);
            } catch (InvocationTargetException ite) {
                Throwable cause = ite.getTargetException();
                if (cause instanceof Exception) {
                    throw (Exception) cause;
                } else {
                    throw new RuntimeException(cause);
                }
            }
        });
    }

    /**
     * Handles the given exception using a registered handler or the default one.
     */
    public static void handle(Throwable ex, HttpExchange exchange) throws Exception {
        ExceptionHandlerInvoker handler = findHandler(ex.getClass());
        handler.handle(ex, exchange);
    }

    private static ExceptionHandlerInvoker findHandler(Class<?> exType) {
        if (handlers.containsKey(exType)) return handlers.get(exType);
        for (Map.Entry<Class<? extends Throwable>, ExceptionHandlerInvoker> entry : handlers.entrySet()) {
            if (entry.getKey().isAssignableFrom(exType)) {
                return entry.getValue();
            }
        }
        return defaultHandler;
    }

    public static void setDefaultHandler(ExceptionHandlerInvoker handler) {
        defaultHandler = handler;
    }
}
