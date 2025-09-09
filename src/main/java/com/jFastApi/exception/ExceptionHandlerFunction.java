package com.jFastApi.exception;

import com.sun.net.httpserver.HttpExchange;

@FunctionalInterface
public interface ExceptionHandlerFunction<T extends Exception> {
    void handle(T ex, HttpExchange exchange) throws Exception;
}
