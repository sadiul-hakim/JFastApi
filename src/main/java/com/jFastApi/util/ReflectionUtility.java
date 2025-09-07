package com.jFastApi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class ReflectionUtility {
    private ReflectionUtility() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ReflectionUtility.class);

    /**
     * Finds all methods annotated with the given annotation in the given package.
     */
    public static List<Method> findAnnotatedMethods(String basePackage, Class<? extends Annotation> annotation) {
        List<Method> methods = new ArrayList<>();
        try {
            for (Class<?> clazz : getClasses(basePackage)) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(annotation)) {
                        methods.add(method);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load annotated methods, error {}", e.getMessage());
        }
        return methods;
    }

    public static List<Class<?>> getAnnotatedClasses(String basePackage, Class<? extends Annotation> annotation) {
        List<Class<?>> classes = new ArrayList<>();
        try {
            for (Class<?> clazz : getClasses(basePackage)) {
                if (clazz.isAnnotationPresent(annotation)) {
                    classes.add(clazz);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Failed to load annotated classes, error {}", e.getMessage());
        }
        return classes;
    }

    // Scans all classes in a package
    private static List<Class<?>> getClasses(String packageName) {

        List<Class<?>> classes = new ArrayList<>();
        try {
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
            List<File> dirs = new ArrayList<>();
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                dirs.add(new File(resource.getFile()));
            }
            for (File directory : dirs) {
                classes.addAll(findClasses(directory, packageName));
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to load classes of package {}, error {}", packageName, ex.getMessage());
        }
        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {

        List<Class<?>> classes = new ArrayList<>();

        try {

            if (!directory.exists()) {
                return classes;
            }
            File[] files = directory.listFiles();
            if (files == null) return classes;

            for (File file : files) {
                if (file.isDirectory()) {
                    classes.addAll(findClasses(file, packageName + "." + file.getName()));
                } else if (file.getName().endsWith(".class")) {
                    String className = packageName + '.' + file.getName().replaceAll("\\.class$", "");
                    classes.add(Class.forName(className));
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to load class of directory {}, error {}", directory.getAbsolutePath(), ex.getMessage());
        }
        return classes;
    }
}
