package com.jFastApi.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.function.Consumer;

/**
 * Generic Hibernate repository for basic CRUD operations.
 *
 * @param <T>  The entity type.
 * @param <ID> The type of the entity's primary key.
 */
public class HibernateRepository<T, ID> {

    private final SessionFactory sessionFactory; // Hibernate SessionFactory to open sessions
    private final Class<T> entityClass;         // The entity class type

    /**
     * Constructor.
     *
     * @param sessionFactory The Hibernate SessionFactory to use.
     * @param entityClass    The entity class managed by this repository.
     */
    public HibernateRepository(SessionFactory sessionFactory, Class<T> entityClass) {
        this.sessionFactory = sessionFactory;
        this.entityClass = entityClass;
    }

    /**
     * Persist the given entity.
     *
     * @param entity The entity to save.
     */
    public void save(T entity) {
        executeTransaction(session -> session.persist(entity));
    }

    /**
     * Remove the given entity.
     *
     * @param entity The entity to delete.
     */
    public void delete(T entity) {
        executeTransaction(session -> session.remove(entity));
    }

    /**
     * Find an entity by its primary key.
     *
     * @param id The primary key.
     * @return The entity if found, or null if not found.
     */
    public T findById(ID id) {
        try (var session = sessionFactory.openSession()) {
            return session.find(entityClass, id);
        }
    }

    /**
     * Retrieve all entities of this type.
     *
     * @return List of all entities.
     */
    public List<T> findAll() {
        try (var session = sessionFactory.openSession()) {
            // Simple HQL query: "from EntityClass"
            return session.createQuery("from " + entityClass.getSimpleName(), entityClass).list();
        }
    }

    /**
     * Executes a transactional action.
     * Handles opening session, starting transaction, committing, and rolling back if an exception occurs.
     *
     * @param action The action to perform inside a transaction.
     */
    private void executeTransaction(Consumer<Session> action) {
        try (var session = sessionFactory.openSession()) {
            var tx = session.beginTransaction();
            try {
                action.accept(session); // Perform the action
                tx.commit();            // Commit transaction if no exception
            } catch (Exception ex) {
                if (tx != null && tx.isActive()) tx.rollback(); // Rollback if exception occurs
                throw ex; // Rethrow to propagate exception
            }
        }
    }
}
