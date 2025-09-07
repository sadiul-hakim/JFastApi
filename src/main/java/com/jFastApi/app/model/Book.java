package com.jFastApi.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Book {

    @Id
    private long id;

    private String name;
}
