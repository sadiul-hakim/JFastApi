package com.jFastApi.app;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class BeanFactory {
    private static final Map<Class<?>, Object> beans = new HashMap<>();

    public static <T> T getBean(Class<T> clazz) {
        try {
            if (beans.containsKey(clazz)) {
                return (T) beans.get(clazz);
            }

            // pick the constructor with the most parameters (like Spring)
            Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                    .max(Comparator.comparingInt(Constructor::getParameterCount))
                    .orElseThrow(() -> new RuntimeException("No public constructor found for " + clazz));

            // resolve constructor parameters
            Object[] params = Arrays.stream(constructor.getParameterTypes())
                    .map(BeanFactory::getBean) // recursive call
                    .toArray();

            // create the instance
            Object instance = constructor.newInstance(params);

            // store in singleton map
            beans.put(clazz, instance);

            return (T) instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create bean: " + clazz, e);
        }
    }
}
