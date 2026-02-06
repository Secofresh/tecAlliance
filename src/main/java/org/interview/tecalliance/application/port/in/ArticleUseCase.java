package org.interview.tecalliance.application.port.in;

import org.interview.tecalliance.domain.model.article.Article;
import org.interview.tecalliance.domain.model.article.BaseArticle;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Article Use Case - Primary port for article management operations.
 * <p>
 * This interface defines the contract for all article-related business operations
 * in the application layer. It follows the hexagonal architecture pattern by
 * providing a port that adapters can use to interact with the domain logic.
 * </p>
 * <p>
 * Implementations of this interface should handle:
 * <ul>
 *   <li>Business rule validation (discount overlaps, price constraints)</li>
 *   <li>Domain object orchestration</li>
 *   <li>Coordination between domain and persistence layers</li>
 * </ul>
 * </p>
 */
public interface ArticleUseCase {

    /**
     * Creates a new article in the system.
     * <p>
     * The article will be validated to ensure:
     * <ul>
     *   <li>No overlapping discount date ranges</li>
     *   <li>Discounts don't cause the price to go below the net price</li>
     * </ul>
     * </p>
     *
     * @param article the article to create (ID will be auto-generated)
     * @return the created article with generated ID
     * @throws IllegalArgumentException if article validation fails
     */
    Article createArticle(Article article);

    /**
     * Retrieves an article by its unique identifier.
     *
     * @param id the article ID to search for
     * @return an Optional containing the article if found, or empty if not found
     * @throws IllegalArgumentException if id is null
     */
    Optional<Article> getArticleById(String id);

    /**
     * Retrieves all articles in the system without any filtering.
     * <p>
     * This method returns basic article information without calculated prices
     * or discount filtering. For filtered results, use {@link #getArticlesWithFilters}.
     * </p>
     *
     * @return list of all articles (maybe empty but never null)
     */
    List<Article> getAllArticles();

    /**
     * Retrieves articles with optional filtering by date, pricing, and discount status.
     * <p>
     * This method provides flexible article retrieval with various filtering options:
     * <ul>
     *   <li>No filters: returns all articles as basic Article objects</li>
     *   <li>withPrices=true: returns ArticleWithPrice with calculated discounted prices</li>
     *   <li>discountOnly=true: returns only articles with active discounts on the given date</li>
     *   <li>Both flags: returns ArticleWithPrice for articles with active discounts only</li>
     * </ul>
     * </p>
     *
     * @param date the date for price/discount calculations (required if withPrices or discountOnly is true)
     * @param withPrices if true, includes calculated prices for the given date
     * @param discountOnly if true, returns only articles with active discounts on the given date
     * @return list of articles (either Article or ArticleWithPrice depending on parameters)
     * @throws IllegalArgumentException if date is null when withPrices or discountOnly is true
     */
    List<BaseArticle> getArticlesWithFilters(LocalDate date, boolean withPrices, boolean discountOnly);

    /**
     * Updates an existing article with new information.
     * <p>
     * All fields of the article will be updated except the ID. The updated article
     * will be validated using the same rules as creation:
     * <ul>
     *   <li>No overlapping discount date ranges</li>
     *   <li>Discounts don't cause the price to go below the net price</li>
     * </ul>
     * </p>
     *
     * @param id the ID of the article to update
     * @param article the article with updated values
     * @return an Optional containing the updated article if found, or empty if not found
     * @throws IllegalArgumentException if article validation fails or id is null
     */
    Optional<Article> updateArticle(String id, Article article);

    /**
     * Deletes an article from the system.
     * <p>
     * This operation permanently removes the article and all its associated data,
     * including discounts.
     * </p>
     *
     * @param id the ID of the article to delete
     * @return true if the article was deleted, false if the article was not found
     * @throws IllegalArgumentException if id is null
     */
    boolean deleteArticle(String id);

    /**
     * Checks if an article exists in the system.
     * <p>
     * This is a lightweight operation that doesn't retrieve the full article data.
     * Useful for validation before performing other operations.
     * </p>
     *
     * @param id the ID to check
     * @return true if an article with the given ID exists, false otherwise
     * @throws IllegalArgumentException if id is null
     */
    boolean existsById(String id);
}
