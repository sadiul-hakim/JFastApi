package com.jFastApi.app.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.function.Consumer;

public class HibernateRepository<T, ID> {

    private final SessionFactory sessionFactory;
    private final Class<T> entityClass;

    public HibernateRepository(SessionFactory sessionFactory, Class<T> entityClass) {
        this.sessionFactory = sessionFactory;
        this.entityClass = entityClass;
    }

    public void save(T entity) {
        executeTransaction(session -> session.persist(entity));
    }

    public void delete(T entity) {
        executeTransaction(session -> session.remove(entity));
    }

    public T findById(ID id) {
        try (var session = sessionFactory.openSession()) {
            return session.find(entityClass, id);
        }
    }

    public List<T> findAll() {
        try (var session = sessionFactory.openSession()) {
            return session.createQuery("from " + entityClass.getSimpleName(), entityClass).list();
        }
    }

    private void executeTransaction(Consumer<Session> action) {
        try (var session = sessionFactory.openSession()) {
            var tx = session.beginTransaction();
            try {
                action.accept(session);
                tx.commit();
            } catch (Exception ex) {
                if (tx != null && tx.isActive()) tx.rollback();
                throw ex;
            }
        }
    }
}
