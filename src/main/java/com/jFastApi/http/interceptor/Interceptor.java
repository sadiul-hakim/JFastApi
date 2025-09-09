package com.jFastApi.http.interceptor;

import com.jFastApi.http.Route;
import com.sun.net.httpserver.HttpExchange;

public interface Interceptor {
    /**
     * Called before the controller method executes.
     *
     * @param exchange the HTTP request/response object
     * @param route the matched route
     * @return true to continue, false to stop the chain (response should be handled manually)
     */
    boolean preHandle(HttpExchange exchange, Route route);

    /**
     * Called after the controller method executes, before sending response.
     *
     * @param exchange the HTTP request/response object
     * @param route the matched route
     * @param result the result returned by controller method
     * @return the object to send back as response (can be modified)
     */
    Object postHandle(HttpExchange exchange, Route route, Object result);

    /**
     * Called if an exception occurs during controller execution.
     *
     * @param exchange the HTTP request/response object
     * @param route the matched route
     * @param ex the exception thrown
     * @return true if exception handled, false to let default error handling run
     */
    boolean onException(HttpExchange exchange, Route route, Throwable ex);
}
