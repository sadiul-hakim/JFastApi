package com.jFastApi.source.model;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Todo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(length = 100, nullable = false)
    private String todo;

    private boolean completed = false;

    public Todo() {
    }

    public Todo(String todo, boolean completed) {
        this.todo = todo;
        this.completed = completed;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTodo() {
        return todo;
    }

    public void setTodo(String todo) {
        this.todo = todo;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof Todo todo1)) return false;

        return id == todo1.id && completed == todo1.completed && Objects.equals(todo, todo1.todo);
    }

    @Override
    public int hashCode() {
        int result = Long.hashCode(id);
        result = 31 * result + Objects.hashCode(todo);
        result = 31 * result + Boolean.hashCode(completed);
        return result;
    }
}
