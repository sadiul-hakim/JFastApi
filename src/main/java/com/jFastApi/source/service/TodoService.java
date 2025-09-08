package com.jFastApi.source.service;

import com.jFastApi.app.AppContext;
import com.jFastApi.app.annotation.Bean;
import com.jFastApi.app.db.HibernateRepository;
import com.jFastApi.source.model.Todo;

import java.util.List;

@Bean
public class TodoService {
    private final HibernateRepository<Todo, Long> todoRepository;

    public TodoService() {
        todoRepository = new HibernateRepository<>(AppContext.getDefaultSessionFactory(), Todo.class);
    }

    public void save(Todo todo) {
        todoRepository.save(todo);
    }

    public List<Todo> findAll() {
        return todoRepository.findAll();
    }
}
