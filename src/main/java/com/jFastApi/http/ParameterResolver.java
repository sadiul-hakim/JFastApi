package com.jFastApi.http;

import com.jFastApi.annotation.RequestBody;
import com.jFastApi.annotation.RequestParam;
import com.jFastApi.exception.ApplicationException;
import com.jFastApi.http.enumeration.HttpMethod;
import com.jFastApi.util.JsonUtility;
import com.jFastApi.util.QueryParamUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;

public final class ParameterResolver {

    private ParameterResolver() {
    }

    /**
     * Resolves the method parameters for a controller method based on annotations.
     * Supports @RequestBody for POST/PUT bodies and @RequestParam for query parameters.
     *
     * @param exchange The HttpExchange object for the incoming request.
     * @param method   The controller method whose parameters need to be resolved.
     * @return An array of objects representing the arguments to pass to the method.
     * @throws IOException If reading the request body fails.
     */
    public static Object[] resolve(HttpExchange exchange, Method method) throws IOException {

        // Get all parameters of the method
        Parameter[] params = method.getParameters();
        int paramsLength = params.length;
        Object[] args = new Object[paramsLength];

        // Determine HTTP method of the request
        HttpMethod httpMethod = HttpMethod.fromString(exchange.getRequestMethod());

        // Loop through all method parameters to resolve their values
        Map<String, String> query = QueryParamUtil.parseQuery(exchange.getRequestURI().getRawQuery());
        for (int i = 0; i < paramsLength; i++) {
            Parameter param = params[i];

            // Handle @RequestBody: deserialize JSON body to parameter type
            if (param.isAnnotationPresent(RequestBody.class)) {
                if (!httpMethod.equals(HttpMethod.POST) && !httpMethod.equals(HttpMethod.PUT)) {
                    throw new ApplicationException("@RequestBody only allowed for POST/PUT methods");
                }
                args[i] = JsonUtility.fromJson(exchange.getRequestBody(), param.getType());
                continue;
            }

            // Handle @RequestParam: extract value from query parameters
            if (param.isAnnotationPresent(RequestParam.class)) {
                RequestParam rp = param.getAnnotation(RequestParam.class);
                String value = query.get(rp.name());
                if (value == null) {
                    // Check if parameter is required or has a default value
                    if (rp.required() && rp.defaultValue().isEmpty()) {
                        throw new ApplicationException("Missing required query param: " + rp.name());
                    }
                    value = rp.defaultValue();
                }

                // Convert String query value to the target parameter type
                args[i] = convertType(value, param.getType());
                continue;
            }

            // If parameter does not have supported annotations, throw an exception
            throw new ApplicationException("Unsupported parameter binding for: " + param);
        }

        return args;
    }


    /**
     * Converts a String value to the specified target type.
     * Supports common primitive types and their wrapper classes.
     *
     * @param value      The String value to convert.
     * @param targetType The target class type to convert to.
     * @return The converted value as an Object of the target type.
     * @throws ApplicationException If the target type is unsupported.
     */
    private static Object convertType(String value, Class<?> targetType) {
        if (value == null) return null; // Null string maps to null

        // String: return as-is
        if (targetType.equals(String.class)) return value;

        // Integer types: parse string to int
        if (targetType.equals(int.class) || targetType.equals(Integer.class))
            return Integer.parseInt(value);

        // Long types: parse string to long
        if (targetType.equals(long.class) || targetType.equals(Long.class))
            return Long.parseLong(value);

        // Boolean types: parse string to boolean (true/false)
        if (targetType.equals(boolean.class) || targetType.equals(Boolean.class))
            return Boolean.parseBoolean(value);

        // Double types: parse string to double
        if (targetType.equals(double.class) || targetType.equals(Double.class))
            return Double.parseDouble(value);

        // Unsupported type → throw exception
        throw new ApplicationException("Unsupported param type: " + targetType.getName());
    }

}

