package org.interview.tecalliance.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.interview.tecalliance.adapter.in.web.dto.ArticleWithPriceDTO;
import org.interview.tecalliance.application.port.in.ArticleUseCase;
import org.interview.tecalliance.domain.model.article.Article;
import org.interview.tecalliance.domain.model.article.ArticleWithPrice;
import org.interview.tecalliance.domain.model.article.BaseArticle;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * REST Adapter (Input Adapter) for Article management.
 * Adapts HTTP requests to use case invocations.
 */
@RestController
@RequestMapping("/api/v1/articles")
@Tag(name = "Article Management", description = "Endpoints for managing articles, pricing, and discounts")
public class ArticleRestAdapter {

    private final ArticleUseCase articleUseCase;

    public ArticleRestAdapter(ArticleUseCase articleUseCase) {
        this.articleUseCase = articleUseCase;
    }

    @PostMapping
    @Operation(
            summary = "Create a new article",
            description = "Creates a new article with the provided details including name, pricing, and VAT information"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Article created successfully",
                    content = @Content(schema = @Schema(implementation = Article.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data"
            )
    })
    public ResponseEntity<Article> createArticle(
            @Parameter(description = "Article data to create", required = true)
            @RequestBody Article article) {
        Article created = articleUseCase.createArticle(article);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(
            summary = "Get all articles",
            description = "Retrieves all articles with optional filtering by date, pricing, and discount status. " +
                    "Can return basic articles, articles with calculated prices, or only articles with active discounts."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved articles",
                    content = @Content(schema = @Schema(implementation = BaseArticle.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid query parameters (e.g., date required when withPrices=true)"
            )
    })
    public ResponseEntity<List<BaseArticle>> getAllArticles(
            @Parameter(description = "Date to calculate prices and discounts (ISO-8601 format: YYYY-MM-DD). Required when withPrices=true or discountOnly=true")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "Include calculated prices for the specified date")
            @RequestParam(required = false, defaultValue = "false") boolean withPrices,
            @Parameter(description = "Return only articles with active discounts on the specified date")
            @RequestParam(required = false, defaultValue = "false") boolean discountOnly) {

        if (withPrices || discountOnly) {
            if (date == null) {
                throw new IllegalArgumentException("Date parameter is required when withPrices=true or discountOnly=true");
            }
            if (withPrices) {
                List<ArticleWithPrice> articlesWithPrices = articleUseCase.getArticlesWithPrices(date);
                if (discountOnly) {
                    articlesWithPrices = articlesWithPrices.stream()
                            .filter(dto -> dto.getAppliedDiscount() != null)
                            .toList();
                }
                // Convert to DTOs for JSON serialization
                List<BaseArticle> baseArticles = articlesWithPrices.stream()
                        .map(ArticleWithPriceDTO::fromDomain)
                        .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                return ResponseEntity.ok(baseArticles);
            } else {
                List<Article> articlesWithDiscount = articleUseCase.getArticlesWithDiscountOn(date);
                List<BaseArticle> baseArticles = new ArrayList<>(articlesWithDiscount);
                return ResponseEntity.ok(baseArticles);
            }
        } else {
            List<Article> articles = articleUseCase.getAllArticles();
            List<BaseArticle> baseArticles = new ArrayList<>(articles);
            return ResponseEntity.ok(baseArticles);
        }
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get article by ID",
            description = "Retrieves a specific article by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Article found",
                    content = @Content(schema = @Schema(implementation = Article.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Article not found"
            )
    })
    public ResponseEntity<Article> getArticleById(
            @Parameter(description = "Article ID", required = true, example = "1")
            @PathVariable Long id) {
        return articleUseCase.getArticleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an article",
            description = "Updates an existing article with new information. Validates that discounts don't overlap and don't reduce price below net price."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Article updated successfully",
                    content = @Content(schema = @Schema(implementation = Article.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid input data or validation error (e.g., overlapping discounts)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Article not found"
            )
    })
    public ResponseEntity<Article> updateArticle(
            @Parameter(description = "Article ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Updated article data", required = true)
            @RequestBody Article article) {
        return articleUseCase.updateArticle(id, article)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Delete an article",
            description = "Deletes an article by its unique identifier"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Article deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Article not found"
            )
    })
    public ResponseEntity<Void> deleteArticle(
            @Parameter(description = "Article ID", required = true, example = "1")
            @PathVariable Long id) {
        if (articleUseCase.deleteArticle(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    @Operation(
            summary = "Check if article exists",
            description = "Checks whether an article exists without returning its data. Uses HTTP HEAD method."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Article exists"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Article does not exist"
            )
    })
    public ResponseEntity<Void> articleExists(
            @Parameter(description = "Article ID", required = true, example = "1")
            @PathVariable Long id) {
        if (articleUseCase.existsById(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
