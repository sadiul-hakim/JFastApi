package com.jFastApi.http.interceptor;

import com.jFastApi.BeanFactory;
import com.jFastApi.annotation.InterceptorBean;
import com.jFastApi.annotation.SystemInterceptorBean;
import com.jFastApi.exception.ApplicationException;
import com.jFastApi.util.ReflectionUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class InterceptorScanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterceptorScanner.class);

    /**
     * Scans the given base package for classes annotated with @InterceptorBean
     * and registers them in the InterceptorRegistry.
     * <p>
     * Responsibilities:
     * - Finds all classes annotated with @InterceptorBean.
     * - Ensures each discovered class implements the Interceptor interface.
     * - Creates bean instances using BeanFactory (so dependencies are injected).
     * - Registers the created interceptors into the global InterceptorRegistry.
     *
     * @param basePackage the root package to scan for interceptor classes
     */
    public static void scanAndRegister(String basePackage, String internalBasePackage) {

        // Find all classes annotated with @InterceptorBean in the given package
        List<Class<?>> classes = ReflectionUtility.findAnnotatedClasses(basePackage, InterceptorBean.class);
        List<Class<?>> internalClasses = ReflectionUtility.findAnnotatedClasses(internalBasePackage, SystemInterceptorBean.class);

        registerInterceptor(classes);
        registerInternalInterceptor(internalClasses);
    }

    private static void registerInterceptor(List<Class<?>> classes) {

        for (Class<?> clazz : classes) {

            InterceptorBean instance = clazz.getAnnotation(InterceptorBean.class);

            // Ensure the discovered class actually implements Interceptor
            if (!Interceptor.class.isAssignableFrom(clazz)) {
                throw new ApplicationException(clazz + " must implement Interceptor");
            }

            try {

                // Create the interceptor instance via BeanFactory (DI container)
                Interceptor interceptor = (Interceptor) BeanFactory.getBeanInstance(clazz);

                // Register the interceptor globally so it applies to all requests
                InterceptorRegistry.register(instance.order(), interceptor);
            } catch (Exception ex) {
                LOGGER.error("Error {}", ex.getMessage());
            }
        }
    }

    private static void registerInternalInterceptor(List<Class<?>> classes) {

        for (Class<?> clazz : classes) {

            SystemInterceptorBean instance = clazz.getAnnotation(SystemInterceptorBean.class);

            // Ensure the discovered class actually implements Interceptor
            if (!Interceptor.class.isAssignableFrom(clazz)) {
                throw new ApplicationException(clazz + " must implement Interceptor");
            }


            try {

                // Create the interceptor instance via BeanFactory (DI container)
                Interceptor interceptor = (Interceptor) BeanFactory.getBeanInstance(clazz);

                // Register the interceptor globally so it applies to all requests
                InterceptorRegistry.registerSystemInterceptor(instance.order(), interceptor);
            } catch (Exception ex) {
                LOGGER.error("Error {}", ex.getMessage());
            }
        }
    }

}
