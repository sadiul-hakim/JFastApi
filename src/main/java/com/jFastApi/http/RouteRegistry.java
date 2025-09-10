package com.jFastApi.http;

import com.jFastApi.enumeration.HttpMethod;
import com.jFastApi.security.RoutePermission;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry for all HTTP routes in the application.
 * Stores mappings from path -> (HTTP method -> Route handler).
 * Provides lookup methods for request dispatching.
 */
final class RouteRegistry {

    // Map of path -> (HTTP method -> Route)
    private static final Map<String, Map<HttpMethod, Route>> routes = new ConcurrentHashMap<>();

    // New: route permissions map
    private static final Map<Route, RoutePermission> permissions = new ConcurrentHashMap<>();

    // Private constructor to prevent instantiation
    private RouteRegistry() {
    }

    /**
     * Registers a new route in the registry.
     *
     * @param route The Route object containing path, method, and handler info.
     */
    static void register(Route route) {
        routes
                .computeIfAbsent(route.path(), k -> new HashMap<>()) // create inner map if path not present
                .put(route.method(), route); // map HTTP method to the Route
    }

    public static void register(Route route, String[] roles) {
        register(route);
        RoutePermission perm = roles.length == 0
                ? RoutePermission.publicRoute()
                : RoutePermission.restricted(Set.of(roles));
        permissions.put(route, perm);
    }

    public static RoutePermission findPermission(Route route) {
        return permissions.getOrDefault(route, RoutePermission.publicRoute());
    }

    /**
     * Finds a registered route by path and HTTP method.
     *
     * @param path   The request path (e.g., "/todo/save").
     * @param method The HTTP method (GET, POST, etc.).
     * @return The Route object if found, or null if not registered.
     */
    static Route find(String path, HttpMethod method) {
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
    static boolean hasPath(String path) {
        return routes.containsKey(path);
    }
}

