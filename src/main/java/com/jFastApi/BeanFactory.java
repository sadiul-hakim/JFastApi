package com.jFastApi;

import com.jFastApi.annotation.Bean;
import com.jFastApi.annotation.InterceptorBean;
import com.jFastApi.annotation.SystemInterceptorBean;
import com.jFastApi.exception.ApplicationException;
import com.jFastApi.util.ReflectionUtility;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * A very simple dependency injection container (BeanFactory).
 * <p>
 * Features:
 * - Supports automatic constructor injection recursively.
 * - Enforces that only classes annotated with @Injectable can be created as beans.
 * - Stores singleton instances so that the same bean is reused across the application.
 * <p>
 * This is a lightweight version of what Spring or Guice does.
 */
public class BeanFactory {

    /**
     * Internal singleton map that holds created bean instances.
     * Key   = Class type
     * Value = Bean instance (singleton)
     */
    private static final Map<Class<?>, Object> beans = new HashMap<>();

    // Map of type (interface or class) -> implementation class
    private static final Map<Class<?>, Class<?>> beanDefinitions = new HashMap<>();

    public static void scanAndRegister(String basePackage, String internalBasePackage) {

        // Use your ReflectionUtility or any classpath scanner
        List<Class<?>> candidates = ReflectionUtility.getClasses(basePackage);
        List<Class<?>> internalCandidates = ReflectionUtility.getClasses(internalBasePackage);

        register(internalCandidates);
        register(candidates);
    }

    private static void register(List<Class<?>> candidates) {

        for (Class<?> clazz : candidates) {

            if (!isBean(clazz)) continue;

            // Register the implementation class itself
            beanDefinitions.put(clazz, clazz);

            // Register all implemented interfaces
            for (Class<?> iface : clazz.getInterfaces()) {
                beanDefinitions.put(iface, clazz);
            }
        }
    }

    private static boolean isBean(Class<?> clazz) {
        if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
            return false;
        }
        return clazz.isAnnotationPresent(Bean.class)
                || clazz.isAnnotationPresent(InterceptorBean.class)
                || clazz.isAnnotationPresent(SystemInterceptorBean.class);
    }

    /**
     * Returns a bean instance for the given class.
     * If the bean does not exist, it is created recursively using
     * its constructor dependencies.
     * <p>
     * Restrictions:
     * - The class must be annotated with @Injectable.
     * - The class must have at least one public constructor.
     * - Constructor dependencies are also resolved via @Injectable beans.
     *
     * @param clazz The class to create or retrieve.
     * @param <T>   The type of the bean.
     * @return The singleton bean instance.
     * @throws RuntimeException if the class is not @Injectable
     *                          or if bean creation fails.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getBeanInstance(Class<T> clazz) {
        try {

            if (!isBean(clazz) && clazz.isInterface()) {
                clazz = (Class<T>) beanDefinitions.get(clazz);
            }

            // Only allow classes explicitly marked as @Injectable
            if (!(clazz.isAnnotationPresent(Bean.class)
                    || clazz.isAnnotationPresent(InterceptorBean.class)
                    || clazz.isAnnotationPresent(SystemInterceptorBean.class))) {
                throw new RuntimeException("No injectable Bean found for Class " + clazz.getName());
            }

            // Return existing singleton instance if already created
            if (beans.containsKey(clazz)) {
                return (T) beans.get(clazz);
            }

            // Look up implementation class
            Class<?> implClass = beanDefinitions.get(clazz);
            if (implClass == null) {
                throw new RuntimeException("No bean definition found for " + clazz.getName());
            }

            // Pick the public constructor with the most parameters
            // (like Spring's "autowiring by constructor")
            String className = clazz.getName();
            Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow(() -> new RuntimeException(
                            "No public constructor found for " + className
                    ));

            // Recursively resolve constructor parameters (dependencies)
            Object[] params = Arrays.stream(constructor.getParameterTypes())
                    .map(BeanFactory::getBeanInstance) // recursive call
                    .toArray();

            // Create the instance via reflection
            Object instance = constructor.newInstance(params);

            // Store in singleton map for future retrieval
            register(clazz, instance);
            register(implClass, instance);

            return (T) instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + clazz, e);
        }
    }

    public static <T> void register(Class<T> clazz, Object instance) {

        if (instance == null) {
            throw new ApplicationException("Null instance is sent to register!");
        }

        // Store in singleton map for future retrieval
        beans.put(clazz, instance);
    }
}