package com.jFastApi.security;

import com.jFastApi.annotation.SystemInterceptorBean;
import com.jFastApi.http.Route;
import com.jFastApi.http.interceptor.Interceptor;
import com.jFastApi.util.PropertiesUtil;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

@SystemInterceptorBean(order = 1) // run before LoggingInterceptor
public class CORSInterceptor implements Interceptor {

    private static final List<String> ALLOWED_ORIGINS = PropertiesUtil.getValueList(PropertiesUtil.SECURITY_ALLOWED_ORIGIN);

    @Override
    public boolean preHandle(HttpExchange exchange, Route route) {
        String origin = exchange.getRequestHeaders().getFirst("Origin");

        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", origin);
            exchange.getResponseHeaders().set("Vary", "Origin"); // important for caches
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type, Authorization");
            exchange.getResponseHeaders().set("Access-Control-Allow-Credentials", "true");
        }

        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            try {
                exchange.sendResponseHeaders(200, -1); // preflight handled here
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return false; // don’t go to controller
        }

        return true; // continue normally
    }

    @Override
    public Object postHandle(HttpExchange exchange, Route route, Object result) {
        return result;
    }

    @Override
    public boolean onException(HttpExchange exchange, Route route, Throwable ex) {

        // You usually don’t change CORS behavior here
        return false;
    }
}

