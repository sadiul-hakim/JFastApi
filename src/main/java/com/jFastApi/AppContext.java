package com.jFastApi;

public final class AppContext {
    private AppContext() {
    }

    private static String basePackage;

    public static void initialize(Class<?> mainClass) {
        basePackage = mainClass.getPackageName();
    }

    public static String getBasePackage() {
        return basePackage;
    }
}
