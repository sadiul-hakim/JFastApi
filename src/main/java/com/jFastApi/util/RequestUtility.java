package com.jFastApi.util;

import com.sun.net.httpserver.HttpExchange;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RequestUtility {

    public static Map<String, String> parseQuery(String query) {

        Map<String, String> map = new HashMap<>();
        if (query == null || query.isEmpty()) return map;

        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            map.put(key, value);
        }
        return map;
    }

    public static Map<String, String> parseFormData(String formData) {
        Map<String, String> map = new HashMap<>();
        String[] pairs = formData.split("&");
        for (String pair : pairs) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                String key = URLDecoder.decode(kv[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(kv[1], StandardCharsets.UTF_8);
                map.put(key, value);
            }
        }
        return map;
    }

    public static String getHeader(HttpExchange exchange, String name) {
        List<String> authHeaders = exchange.getRequestHeaders().get(name);
        if (authHeaders != null && !authHeaders.isEmpty()) {
            return authHeaders.getFirst(); // usually only one Authorization header
        }
        return null; // header not present
    }

    /**
     * Get a trustable client IP address.
     * Checks "X-Forwarded-For" first (if behind proxy),
     * falls back to direct connection if header is missing.
     */
    public static String getClientIp(HttpExchange exchange) {
        // Check common proxy header
        String ip = exchange.getRequestHeaders().getFirst("X-Forwarded-For");
        if (ip != null && !ip.isBlank()) {
            // X-Forwarded-For may contain multiple IPs, client is the first
            return ip.split(",")[0].trim();
        }

        // Fallback to direct connection
        if (exchange.getRemoteAddress() != null && exchange.getRemoteAddress().getAddress() != null) {
            return exchange.getRemoteAddress().getAddress().getHostAddress();
        }

        return "unknown";
    }
}
