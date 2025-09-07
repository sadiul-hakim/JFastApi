package com.jFastApi.app;

import org.hibernate.SessionFactory;

public final class AppContext {
    private AppContext() {
    }

    private static String basePackage;

    private static SessionFactory defaultSessionFactory;

    public static void initialize(Class<?> mainClass) {
        basePackage = mainClass.getPackageName();
    }

    public static String getBasePackage() {
        return basePackage;
    }

    public static SessionFactory getDefaultSessionFactory() {
        return defaultSessionFactory;
    }

    public static void setDefaultSessionFactory(SessionFactory defaultSessionFactory) {
        AppContext.defaultSessionFactory = defaultSessionFactory;
    }
}
