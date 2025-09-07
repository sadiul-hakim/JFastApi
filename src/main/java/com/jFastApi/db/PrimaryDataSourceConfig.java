package com.jFastApi.db;

import com.jFastApi.app.AppContext;
import com.jFastApi.util.PropertiesUtil;
import com.jFastApi.util.ReflectionUtility;
import jakarta.persistence.Entity;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.List;
import java.util.Map;

public final class PrimaryDataSourceConfig {
    private PrimaryDataSourceConfig() {
    }

    private static final String URL = "app.datasource.url";
    private static final String USERNAME = "app.datasource.username";
    private static final String PASSWORD = "app.datasource.password";
    private static final String DRIVER = "app.datasource.driver-class-name";
    private static final String HIBERNATE_PROPERTIES_PREFIX = "app.datasource.hibernate.";
    private static final String HIBERNATE_PROPERTIES_KEY = "hibernate";

    private static final String HIBERNATE_URL = "hibernate.connection.url";
    private static final String HIBERNATE_USERNAME = "hibernate.connection.username";
    private static final String HIBERNATE_PASSWORD = "hibernate.connection.password";
    private static final String HIBERNATE_DRIVER = "hibernate.connection.driver_class";

    private static final SessionFactory defaultSessionFactory;

    static {
        Map<String, String> hibernateProperties = PropertiesUtil.getAllByPrefix(HIBERNATE_PROPERTIES_PREFIX,
                HIBERNATE_PROPERTIES_KEY);
        String uri = PropertiesUtil.getProperty(URL);
        String username = PropertiesUtil.getProperty(USERNAME);
        String password = PropertiesUtil.getProperty(PASSWORD);
        String driver = PropertiesUtil.getProperty(DRIVER);

        Configuration cfg = new Configuration();

        // Required JDBC connection settings
        cfg.setProperty(HIBERNATE_DRIVER, driver);
        cfg.setProperty(HIBERNATE_URL, uri);
        cfg.setProperty(HIBERNATE_USERNAME, username);
        cfg.setProperty(HIBERNATE_PASSWORD, password);

        // Hibernate settings
        hibernateProperties.forEach(cfg::setProperty);

        List<Class<?>> entities = ReflectionUtility.getAnnotatedClasses(AppContext.getBasePackage(), Entity.class);
        entities.forEach(cfg::addAnnotatedClass);

        defaultSessionFactory = cfg.buildSessionFactory();
    }

    public static SessionFactory getDefaultSessionFactory() {
        return defaultSessionFactory;
    }
}
