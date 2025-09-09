package com.jFastApi.exception;

import com.sun.net.httpserver.HttpExchange;

@FunctionalInterface
public interface ExceptionHandlerInvoker {
    void handle(Throwable ex, HttpExchange exchange) throws Exception;
}
