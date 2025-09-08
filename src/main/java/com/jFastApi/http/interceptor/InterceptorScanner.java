package com.jFastApi.http.interceptor;

import com.jFastApi.BeanFactory;
import com.jFastApi.annotation.InterceptorBean;
import com.jFastApi.util.ReflectionUtility;

import java.util.List;

public class InterceptorScanner {

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
    public static void scanAndRegister(String basePackage) {

        // Find all classes annotated with @InterceptorBean in the given package
        List<Class<?>> classes = ReflectionUtility.findAnnotatedClasses(basePackage, InterceptorBean.class);

        for (Class<?> clazz : classes) {

            // Ensure the discovered class actually implements Interceptor
            if (!Interceptor.class.isAssignableFrom(clazz)) {
                throw new RuntimeException(clazz + " must implement Interceptor");
            }

            // Create the interceptor instance via BeanFactory (DI container)
            Interceptor interceptor = (Interceptor) BeanFactory.getBean(clazz);

            // Register the interceptor globally so it applies to all requests
            InterceptorRegistry.register(interceptor);
        }
    }

}
