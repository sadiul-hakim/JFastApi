package com.jFastApi.app;

import com.jFastApi.app.annotation.Bean;
import com.jFastApi.app.annotation.InterceptorBean;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

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
    public static <T> T getBean(Class<T> clazz) {
        try {

            // Only allow classes explicitly marked as @Injectable
            if (!(clazz.isAnnotationPresent(Bean.class) || clazz.isAnnotationPresent(InterceptorBean.class))) {
                throw new RuntimeException(
                        "No injectable Bean found for Class " + clazz.getName()
                );
            }

            // Return existing singleton instance if already created
            if (beans.containsKey(clazz)) {
                return (T) beans.get(clazz);
            }

            // Pick the public constructor with the most parameters
            // (like Spring's "autowiring by constructor")
            Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow(() -> new RuntimeException(
                            "No public constructor found for " + clazz
                    ));

            // Recursively resolve constructor parameters (dependencies)
            Object[] params = Arrays.stream(constructor.getParameterTypes())
                    .map(BeanFactory::getBean) // recursive call
                    .toArray();

            // Create the instance via reflection
            Object instance = constructor.newInstance(params);

            // Store in singleton map for future retrieval
            beans.put(clazz, instance);

            return (T) instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + clazz, e);
        }
    }
}