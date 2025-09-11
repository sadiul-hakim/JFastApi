package com.jFastApi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jFastApi.http.Response;
import com.jFastApi.enumeration.ContentType;
import com.jFastApi.enumeration.HttpStatus;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class ResponseUtility {
    private ResponseUtility() {
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * Sends an error response back to the client in JSON format.
     *
     * @param ex         The exception that occurred.
     * @param exchange   The HttpExchange object for sending the response.
     * @param statusCode The HTTP status code to send (e.g., 400, 404, 500).
     */
    public static void sendErrorResponse(Exception ex, HttpExchange exchange, HttpStatus statusCode) {

        // Create a simple map with a "message" field containing the exception message
        Map<String, String> response = new HashMap<>();
        response.put("message", ex.getMessage());

        // Convert the map to a JSON string
        String body = JsonUtility.toJson(response);

        // Send the JSON response with the provided status code and content type
        ResponseUtility.sendResponse(exchange, statusCode, ContentType.JSON, body);
    }

    /**
     * Sends the response returned by a controller method.
     * Handles both Response<T> objects and void/other types.
     *
     * @param result   The object returned by the controller method.
     * @param exchange The HttpExchange object for sending the response.
     */
    public static void sendResponse(Object result, HttpExchange exchange) {
        try {
            if (result instanceof Response<?> response) {
                // If the result is a Response object, delegate to sendResponse
                ResponseUtility.sendResponse(exchange, response);
            } else {
                // Optional: allow void or unrecognized return types
                // Send 204 No Content if controller returned nothing meaningful
                exchange.sendResponseHeaders(204, -1);
                exchange.close();
            }
        } catch (IOException e) {
            // Wrap IOException as unchecked to propagate it
            throw new RuntimeException(e);
        }
    }


    /**
     * Convenience overload using enums
     */
    public static void sendResponse(HttpExchange exchange, HttpStatus status, ContentType contentType, String body) {
        sendResponse(exchange, status.getCode(), contentType.getMimeType(), body);
    }

    public static void sendResponse(HttpExchange exchange, HttpStatus status, ContentType contentType, Object body) {
        String json = JsonUtility.toJson(body);
        sendResponse(exchange, status.getCode(), contentType.getMimeType(), json);
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

    /**
     * Sends a Response<T> back to the client using the HttpExchange.
     * Supports String, byte[], and JSON bodies automatically.
     *
     * @param exchange The HttpExchange object for sending the response.
     * @param response The Response<T> object containing body, headers, status, and content type.
     * @param <T>      The type of the response body.
     */
    public static <T> void sendResponse(HttpExchange exchange, Response<T> response) {
        byte[] bytes;

        try {
            // Convert body to bytes depending on type
            if (response.getBody() == null) {
                // No body → send empty response
                bytes = new byte[0];
            } else if (response.getBody() instanceof String s) {
                // Body is a plain String → convert to UTF-8 bytes
                bytes = s.getBytes(StandardCharsets.UTF_8);
            } else if (response.getBody() instanceof byte[] b) {
                // Body is already a byte array → use directly
                bytes = b;
            } else if (response.getContentType() == ContentType.JSON) {
                // Auto-convert any object to JSON using Jackson ObjectMapper
                bytes = OBJECT_MAPPER.writeValueAsBytes(response.getBody());
            } else {
                // Unsupported body type → throw exception
                throw new IllegalArgumentException(
                        "Unsupported response body type: " + response.getBody().getClass()
                );
            }

            // Set Content-Type header
            exchange.getResponseHeaders().add("Content-Type", response.getContentType().getMimeType());

            // Set any additional custom headers
            for (Map.Entry<String, String> entry : response.getHeaders().entrySet()) {
                exchange.getResponseHeaders().add(entry.getKey(), entry.getValue());
            }

            // Handle Connection: keep-alive if requested
            if (response.isKeepAlive()) {
                exchange.getResponseHeaders().add("Connection", "keep-alive");
            }

            // Send response headers with status code and body length
            exchange.sendResponseHeaders(response.getStatus().getCode(), bytes.length);

            // Write the response body
            exchange.getResponseBody().write(bytes);

        } catch (IOException e) {
            // Log error and attempt to send fallback 500 response
            System.err.println("Error sending response: " + e);
            try {
                exchange.sendResponseHeaders(500, 0);
            } catch (IOException ex) {
                System.err.println("Failed to send fallback 500: " + ex);
            }
        } finally {
            // Ensure exchange is closed to free resources
            try {
                exchange.close();
            } catch (Exception ignored) {
            }
        }
    }
}
