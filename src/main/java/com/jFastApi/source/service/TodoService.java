package com.jFastApi.source.service;

import com.jFastApi.source.model.Todo;
import com.jFastApi.db.HibernateRepository;
import com.jFastApi.db.PrimaryDataSourceConfig;

import java.util.List;

public class TodoService {
    private final HibernateRepository<Todo, Long> todoRepository;

    public TodoService() {
        todoRepository = new HibernateRepository<>(PrimaryDataSourceConfig.getDefaultSessionFactory(), Todo.class);
    }

    public void save(Todo todo) {
        todoRepository.save(todo);
    }

    public List<Todo> findAll() {
        return todoRepository.findAll();
    }
}
