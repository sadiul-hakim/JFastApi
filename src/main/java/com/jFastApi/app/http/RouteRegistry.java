package com.jFastApi.app.http;

import com.jFastApi.app.http.enumeration.HttpMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for all HTTP routes in the application.
 * Stores mappings from path -> (HTTP method -> Route handler).
 * Provides lookup methods for request dispatching.
 */
public final class RouteRegistry {

    // Map of path -> (HTTP method -> Route)
    private static final Map<String, Map<HttpMethod, Route>> routes = new HashMap<>();

    // Private constructor to prevent instantiation
    private RouteRegistry() {
    }

    /**
     * Registers a new route in the registry.
     *
     * @param def The Route object containing path, method, and handler info.
     */
    public static void register(Route def) {
        routes
                .computeIfAbsent(def.path(), k -> new HashMap<>()) // create inner map if path not present
                .put(def.method(), def); // map HTTP method to the Route
    }

    /**
     * Finds a registered route by path and HTTP method.
     *
     * @param path   The request path (e.g., "/todo/save").
     * @param method The HTTP method (GET, POST, etc.).
     * @return The Route object if found, or null if not registered.
     */
    public static Route find(String path, HttpMethod method) {
        Map<HttpMethod, Route> methodMap = routes.get(path);
        if (methodMap == null) return null; // path not found
        return methodMap.get(method); // return route for method or null
    }

    /**
     * Checks whether any route exists for the given path.
     * Useful for returning 404 vs 405.
     *
     * @param path The request path to check.
     * @return True if the path exists in the registry, false otherwise.
     */
    public static boolean hasPath(String path) {
        return routes.containsKey(path);
    }
}

