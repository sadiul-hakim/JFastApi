package com.jFastApi.app.controller;

import com.jFastApi.annotation.HttpRoute;
import com.jFastApi.http.Response;
import com.jFastApi.http.enumeration.ContentType;
import com.jFastApi.http.enumeration.HttpMethod;
import com.jFastApi.http.enumeration.HttpStatus;
import com.sun.net.httpserver.HttpExchange;

import java.util.Map;

public class TestController {

    @HttpRoute(path = "/ping", method = HttpMethod.GET)
    public Response<Map> pingPong(HttpExchange exchange) {
        return new Response.Builder<Map>()
                .contentType(ContentType.JSON)
                .status(HttpStatus.OK)
                .body(Map.of("name","Hakim"))
                .build();
    }
}
