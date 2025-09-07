package com.jFastApi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jFastApi.http.Response;
import com.jFastApi.http.enumeration.ContentType;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class ResponseUtility {
    private ResponseUtility() {
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();


    public static <T> void sendResponse(HttpExchange exchange, Response<T> response) {
        byte[] bytes;

        try {
            // Convert body to bytes
            if (response.getBody() == null) {
                bytes = new byte[0];
            } else if (response.getBody() instanceof String s) {
                bytes = s.getBytes(StandardCharsets.UTF_8);
            } else if (response.getBody() instanceof byte[] b) {
                bytes = b;
            } else if (response.getContentType() == ContentType.JSON) {
                // Auto-convert object to JSON
                bytes = OBJECT_MAPPER.writeValueAsBytes(response.getBody());
            } else {
                throw new IllegalArgumentException(
                        "Unsupported response body type: " + response.getBody().getClass()
                );
            }

            // Set headers
            exchange.getResponseHeaders().add("Content-Type", response.getContentType().getMimeType());
            for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
                exchange.getResponseHeaders().add(entry.getKey(), entry.getValue());
            }

            if (response.isKeepAlive()) {
                exchange.getResponseHeaders().add("Connection", "keep-alive");
            }

            // Send headers and body
            exchange.sendResponseHeaders(response.getStatus().getCode(), bytes.length);
            exchange.getResponseBody().write(bytes);

        } catch (IOException e) {
            System.err.println("Error sending response: " + e);
            try {
                exchange.sendResponseHeaders(500, 0);
            } catch (IOException ex) {
                System.err.println("Failed to send fallback 500: " + ex);
            }
        } finally {
            try {
                exchange.close();
            } catch (Exception ignored) {}
        }
    }
}
