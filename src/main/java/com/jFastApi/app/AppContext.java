package com.jFastApi.app;

import org.hibernate.SessionFactory;

/**
 * Holds global application context for the mini framework.
 * Stores base package for scanning and default Hibernate SessionFactory.
 */
public final class AppContext {

    private AppContext() {
        // Private constructor to prevent instantiation
    }

    /**
     * Base package of the application, determined from the main class
     */
    private static String basePackage;

    /**
     * Default Hibernate SessionFactory, initialized in PrimaryDataSourceConfig
     */
    private static SessionFactory defaultSessionFactory;

    /**
     * Initializes the application context with the main class.
     * Extracts the base package to be used for classpath scanning.
     *
     * @param mainClass The application's main class.
     */
    public static void initialize(Class<?> mainClass) {
        basePackage = mainClass.getPackageName();
    }

    /**
     * Returns the base package of the application.
     *
     * @return The base package string.
     */
    public static String getBasePackage() {
        return basePackage;
    }

    /**
     * Returns the default Hibernate SessionFactory.
     *
     * @return The SessionFactory instance.
     */
    public static SessionFactory getDefaultSessionFactory() {
        return defaultSessionFactory;
    }

    /**
     * Sets the default Hibernate SessionFactory.
     * Called from PrimaryDataSourceConfig after building the SessionFactory.
     *
     * @param defaultSessionFactory The SessionFactory to set as default.
     */
    public static void setDefaultSessionFactory(SessionFactory defaultSessionFactory) {
        AppContext.defaultSessionFactory = defaultSessionFactory;
    }
}
