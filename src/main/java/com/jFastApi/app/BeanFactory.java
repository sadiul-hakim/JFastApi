package com.jFastApi.app;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple dependency injection container (BeanFactory).
 * Supports automatic constructor injection recursively (like Spring).
 */
public class BeanFactory {

    /**
     * Singleton map to store created bean instances
     */
    private static final Map<Class<?>, Object> beans = new HashMap<>();

    /**
     * Returns a bean instance for the given class.
     * If the bean does not exist, it is created recursively using its constructor dependencies.
     *
     * @param clazz The class to create or retrieve.
     * @param <T>   The type of the bean.
     * @return The singleton bean instance.
     */
    public static <T> T getBean(Class<T> clazz) {
        try {
            // Return existing instance if already created
            if (beans.containsKey(clazz)) {
                return (T) beans.get(clazz);
            }

            // Pick the public constructor with the most parameters (similar to Spring's autowiring)
            Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow(() -> new RuntimeException("No public constructor found for " + clazz));

            // Recursively resolve constructor parameters
            Object[] params = Arrays.stream(constructor.getParameterTypes())
                    .map(BeanFactory::getBean) // recursive call
                    .toArray();

            // Instantiate the class with resolved parameters
            Object instance = constructor.newInstance(params);

            // Store in singleton map for future calls
            beans.put(clazz, instance);

            return (T) instance;

        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + clazz, e);
        }
    }
}
