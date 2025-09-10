package com.jFastApi.exception;

import com.jFastApi.enumeration.ContentType;
import com.jFastApi.enumeration.HttpStatus;
import com.jFastApi.util.ResponseUtility;
import com.sun.net.httpserver.HttpExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DefaultGlobalExceptionHandler implements ExceptionHandlerInvoker {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultGlobalExceptionHandler.class);

    @Override
    public void handle(Throwable ex, HttpExchange exchange) {
        LOGGER.error("Unhandled exception: {}", ex.getMessage());

        try (exchange) {
            String json = "{\"error\":\"" + ex.getMessage() + "\"}";
            ResponseUtility.sendResponse(exchange, HttpStatus.INTERNAL_SERVER_ERROR, ContentType.JSON, json);
        }
    }
}
