package org.interview.tecalliance.application.port.out;

import org.interview.tecalliance.domain.model.article.Article;

import java.util.List;
import java.util.Optional;

/**
 * Article Persistence Port - Output port for article data persistence operations.
 * <p>
 * This interface defines the contract for persisting and retrieving article data
 * in the hexagonal architecture. It serves as an abstraction layer between the
 * application core and the infrastructure/persistence layer.
 * </p>
 * <p>
 * Implementations of this port can use various persistence mechanisms:
 * <ul>
 *   <li>Relational databases (JPA, JDBC)</li>
 *   <li>NoSQL databases (MongoDB, Cassandra)</li>
 *   <li>In-memory storage</li>
 *   <li>External services (REST APIs, gRPC)</li>
 * </ul>
 * </p>
 * <p>
 * This port follows the Dependency Inversion Principle - the application layer
 * depends on this abstraction, not on concrete persistence implementations.
 * </p>
 */
public interface ArticlePersistencePort {

    /**
     * Saves an article to the persistent storage.
     * <p>
     * If the article has no ID (null), a new article will be created and a unique
     * ID will be generated. If the article has an existing ID, the article will be
     * updated with the provided data.
     * </p>
     * <p>
     * This method performs an upsert operation (insert or update).
     * </p>
     *
     * @param article the article to save (must not be null)
     * @return the saved article with generated/updated ID and any auto-generated fields
     * @throws IllegalArgumentException if article is null
     * @throws org.springframework.dao.DataAccessException if persistence operation fails
     */
    Article save(Article article);

    /**
     * Retrieves an article by its unique identifier.
     * <p>
     * This is a read-only operation that does not modify the persistent storage.
     * </p>
     *
     * @param id the unique identifier of the article to retrieve (must not be null)
     * @return an Optional containing the article if found, or empty if not found
     * @throws IllegalArgumentException if id is null
     * @throws org.springframework.dao.DataAccessException if persistence operation fails
     */
    Optional<Article> findById(String id);

    /**
     * Retrieves all articles from the persistent storage.
     * <p>
     * This method loads all articles without any filtering or pagination.
     * For large datasets, consider implementing pagination or filtering mechanisms.
     * </p>
     * <p>
     * The returned list is modifiable but changes will not affect the persistent storage
     * unless explicitly saved.
     * </p>
     *
     * @return a list of all articles (may be empty but never null)
     * @throws org.springframework.dao.DataAccessException if persistence operation fails
     */
    List<Article> findAll();

    /**
     * Deletes an article from the persistent storage by its unique identifier.
     * <p>
     * This operation permanently removes the article and all its associated data
     * (including discounts) from the storage. This action cannot be undone.
     * </p>
     * <p>
     * If the article does not exist, this method returns false without throwing an exception.
     * </p>
     *
     * @param id the unique identifier of the article to delete (must not be null)
     * @return true if the article was deleted, false if the article was not found
     * @throws IllegalArgumentException if id is null
     * @throws org.springframework.dao.DataAccessException if persistence operation fails
     */
    boolean deleteById(String id);

    /**
     * Checks whether an article with the given identifier exists in the persistent storage.
     * <p>
     * This is a lightweight operation that only checks for existence without
     * loading the full article data. Useful for validation operations.
     * </p>
     * <p>
     * This method is more efficient than loading the article and checking for null,
     * especially when the article entity is large or has many relationships.
     * </p>
     *
     * @param id the unique identifier to check (must not be null)
     * @return true if an article with the given ID exists, false otherwise
     * @throws IllegalArgumentException if id is null
     * @throws org.springframework.dao.DataAccessException if persistence operation fails
     */
    boolean existsById(String id);
}
