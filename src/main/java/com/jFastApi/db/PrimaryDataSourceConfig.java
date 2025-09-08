package com.jFastApi.db;

import com.jFastApi.AppContext;
import com.jFastApi.exception.ApplicationException;
import com.jFastApi.util.PropertiesUtil;
import com.jFastApi.util.ReflectionUtility;
import jakarta.persistence.Entity;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Initializes the default Hibernate SessionFactory from application properties.
 * No persistence.xml or hibernate.properties file is required.
 */
public final class PrimaryDataSourceConfig {

    private PrimaryDataSourceConfig() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PrimaryDataSourceConfig.class);

    // Property keys for datasource
    private static final String URL = "app.datasource.url";
    private static final String USERNAME = "app.datasource.username";
    private static final String PASSWORD = "app.datasource.password";
    private static final String DRIVER = "app.datasource.driver-class-name";

    // Prefix for hibernate-specific properties in app.properties
    private static final String HIBERNATE_PROPERTIES_PREFIX = "app.datasource.hibernate.";
    private static final String HIBERNATE_PROPERTIES_KEY = "hibernate";

    // Hibernate property keys
    private static final String HIBERNATE_URL = "hibernate.connection.url";
    private static final String HIBERNATE_USERNAME = "hibernate.connection.username";
    private static final String HIBERNATE_PASSWORD = "hibernate.connection.password";
    private static final String HIBERNATE_DRIVER = "hibernate.connection.driver_class";

    public static void init() {
        try {
            // Load all Hibernate properties from app.properties starting with the prefix
            Map<String, String> hibernateProperties = PropertiesUtil.getAllByPrefix(
                    HIBERNATE_PROPERTIES_PREFIX, HIBERNATE_PROPERTIES_KEY
            );

            // Load basic datasource properties
            String uri = PropertiesUtil.getProperty(URL);
            String username = PropertiesUtil.getProperty(USERNAME);
            String password = PropertiesUtil.getProperty(PASSWORD);
            String driver = PropertiesUtil.getProperty(DRIVER);

            // Validate required properties
            if (uri == null || driver == null) {
                throw new ApplicationException("Database not configured. Missing URL or driver-class-name.");
            }

            // Try loading the JDBC driver explicitly
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException e) {
                throw new ApplicationException("JDBC Driver not found: " + driver);
            }

            // Create Hibernate Configuration programmatically
            Configuration cfg = new Configuration();

            // Required JDBC connection settings
            cfg.setProperty(HIBERNATE_DRIVER, driver);
            cfg.setProperty(HIBERNATE_URL, uri);
            cfg.setProperty(HIBERNATE_USERNAME, username != null ? username : "");
            cfg.setProperty(HIBERNATE_PASSWORD, password != null ? password : "");

            // Apply any additional Hibernate settings from properties
            hibernateProperties.forEach(cfg::setProperty);

            // Scan for @Entity annotated classes in the base package
            List<Class<?>> entities = ReflectionUtility.findAnnotatedClasses(
                    AppContext.getBasePackage(), Entity.class
            );
            entities.forEach(cfg::addAnnotatedClass);

            // Build the SessionFactory and set it as the default in AppContext
            SessionFactory sessionFactory = cfg.buildSessionFactory();
            AppContext.setDefaultSessionFactory(sessionFactory);

            LOGGER.info("Hibernate SessionFactory initialized successfully.");

        } catch (Exception ex) {
            LOGGER.error("Failed to initialize Default SessionFactory", ex);
            throw new ApplicationException("Hibernate initialization failed");
        }
    }
}

