package com.jFastApi.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public final class PropertiesUtil {
    private PropertiesUtil() {
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertiesUtil.class);
    private static final String PROPERTIES_FILE_NAME = "app.properties";
    private static Properties PROPERTIES;

    static {

        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            if (classLoader == null) {
                classLoader = PropertiesUtil.class.getClassLoader();
            }

            try (InputStream is = classLoader.getResourceAsStream(PROPERTIES_FILE_NAME)) {
                PROPERTIES = new Properties();
                PROPERTIES.load(is);
            } catch (IOException ex) {
                LOGGER.error("Failed to read properties from app.properties file!");
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to properties file, error {}", ex.getMessage());
        }
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static int getPropertyInteger(String key) {
        String property = PROPERTIES.getProperty(key);

        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public static int getPropertyInteger(String key, int defaultValue) {
        String property = PROPERTIES.getProperty(key);

        try {
            return Integer.parseInt(property);
        } catch (NumberFormatException ex) {
            LOGGER.error("Failed to parse {} into integer", property);
        }

        return defaultValue;
    }

    public static Map<String, String> getAllByPrefix(String prefix, String propertyKey) {
        Map<String, String> result = new HashMap<>();

        for (String key : PROPERTIES.stringPropertyNames()) {
            if (key.startsWith(prefix)) {

                // Strip the prefix
                String extractedKey = key.substring(key.indexOf(propertyKey));
                result.put(extractedKey, PROPERTIES.getProperty(key));
            }
        }

        return result;
    }

}
