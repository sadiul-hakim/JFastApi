package com.jFastApi.source.controller;

import com.jFastApi.app.annotation.Bean;
import com.jFastApi.app.annotation.HttpRoute;
import com.jFastApi.app.annotation.RequestBody;
import com.jFastApi.app.annotation.RequestParam;
import com.jFastApi.app.http.Response;
import com.jFastApi.app.http.enumeration.ContentType;
import com.jFastApi.app.http.enumeration.HttpMethod;
import com.jFastApi.app.http.enumeration.HttpStatus;
import com.jFastApi.source.model.Todo;
import com.jFastApi.source.service.TodoService;

import java.util.List;
import java.util.Map;

@Bean
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @HttpRoute(path = "/todo/save", method = HttpMethod.POST)
    public Response<Map> saveTodo(@RequestBody Todo todo) {

        todoService.save(todo);

        return new Response.Builder<Map>()
                .contentType(ContentType.JSON)
                .status(HttpStatus.OK)
                .body(Map.of("message", "Todo is saved successfully!"))
                .build();
    }

    @HttpRoute(path = "/todo/find-all", method = HttpMethod.GET)
    public Response<List> saveTodo(@RequestParam(name = "pageNumber", defaultValue = "0", required = false) long pageNumber) {

        List<Todo> list = todoService.findAll();
        return new Response.Builder<List>()
                .contentType(ContentType.JSON)
                .status(HttpStatus.OK)
                .body(list)
                .build();
    }
}
