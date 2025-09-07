package com.jFastApi.app.http.enumeration;

public enum HttpMethod {
    GET,
    POST,
    PUT,
    DELETE,
    PATCH,
    HEAD,
    OPTIONS,
    TRACE,
    CONNECT;

    /**
     * Optional: Lookup enum from string (case-insensitive)
     */
    public static HttpMethod fromString(String method) {
        for (HttpMethod m : values()) {
            if (m.name().equalsIgnoreCase(method)) {
                return m;
            }
        }
        throw new IllegalArgumentException("Unknown HTTP method: " + method);
    }
}

