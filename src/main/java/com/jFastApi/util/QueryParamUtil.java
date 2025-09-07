package com.jFastApi.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class QueryParamUtil {

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
}
