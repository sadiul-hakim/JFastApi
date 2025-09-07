package com.jFastApi.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jFastApi.app.exception.ApplicationException;
import com.jFastApi.app.http.Response;
import com.jFastApi.app.http.enumeration.ContentType;
import com.jFastApi.app.http.enumeration.HttpStatus;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class ResponseUtility {
    private ResponseUtility() {
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void sendErrorResponse(Exception ex, HttpExchange exchange) {

        try {

            Map<String, String> response = new HashMap<>();
            response.put("message", ex.getMessage());

            String body = JsonUtility.toJson(response);

            if (ex instanceof ApplicationException ignore) {
                ResponseUtility.sendResponse(exchange, HttpStatus.BAD_REQUEST, ContentType.JSON, body);
            } else {
                ResponseUtility.sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, body);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sendResponse(Object result, HttpExchange exchange) {

        try {

            if (result instanceof Response<?> response) {
                ResponseUtility.sendResponse(exchange, response);
            } else {

                // optional: allow void methods too
                exchange.sendResponseHeaders(204, -1); // No Content
                exchange.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convenience overload using enums
     */
    public static void sendResponse(HttpExchange exchange, HttpStatus status, ContentType contentType, String body) {
        sendResponse(exchange, status.getCode(), contentType.getMimeType(), body);
    }

    /**
     * Send a response to the client.
     *
     * @param exchange    HttpExchange object
     * @param statusCode  HTTP status code (e.g., 200)
     * @param contentType MIME type of the response (e.g., "text/plain")
     * @param body        Response body as a String
     */
    public static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String body) {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        sendResponse(exchange, statusCode, contentType, bytes);
    }

    /**
     * Send a response to the client (byte array version).
     *
     * @param exchange    HttpExchange object
     * @param statusCode  HTTP status code
     * @param contentType MIME type
     * @param bytes       Response body as byte[]
     */
    public static void sendResponse(HttpExchange exchange, int statusCode, String contentType, byte[] bytes) {
        try {
            // Set headers
            exchange.getResponseHeaders().add("Content-Type", contentType);

            // Send status code and length
            exchange.sendResponseHeaders(statusCode, bytes.length);

            // Write body
            var out = exchange.getResponseBody();
            out.write(bytes);
        } catch (IOException e) {
            System.err.println("Error sending response: " + e);
            try {
                exchange.sendResponseHeaders(500, 0); // Fallback to 500
            } catch (IOException ex) {
                System.err.println("Failed to send 500 response: " + ex);
            }
        } finally {
            try {
                exchange.close();
            } catch (Exception ignored) {
            }
        }
    }

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
            } catch (Exception ignored) {
            }
        }
    }
}
