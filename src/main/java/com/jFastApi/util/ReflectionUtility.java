package com.jFastApi.util;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public final class ReflectionUtility {
    private ReflectionUtility() {
    }

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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
        return classes;
    }

    // Scans all classes in a package
    private static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException {

        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }
        List<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {

        List<Class<?>> classes = new ArrayList<>();
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
        return classes;
    }
}
