package org.interview.tecalliance.controller;

import org.interview.tecalliance.dto.ArticleWithPriceDTO;
import org.interview.tecalliance.model.Article;
import org.interview.tecalliance.model.BaseArticle;
import org.interview.tecalliance.service.ArticleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @PostMapping
    public ResponseEntity<Article> createArticle(@RequestBody Article article) {
        Article created = articleService.createArticle(article);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<BaseArticle>> getAllArticles(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false, defaultValue = "false") boolean withPrices,
            @RequestParam(required = false, defaultValue = "false") boolean discountOnly) {

        if (withPrices || discountOnly) {
            if (date == null) {
                throw new IllegalArgumentException("Date parameter is required when withPrices=true or discountOnly=true");
            }
            if (withPrices) {
                List<ArticleWithPriceDTO> articlesWithPrices = articleService.getArticlesWithPrices(date);
                if (discountOnly) {
                    articlesWithPrices = articlesWithPrices.stream()
                            .filter(dto -> dto.getAppliedDiscount() != null)
                            .toList();
                }
                // Cast to List<BaseArticle>
                List<BaseArticle> baseArticles = new ArrayList<>(articlesWithPrices);
                return ResponseEntity.ok(baseArticles);
            } else {
                List<Article> articlesWithDiscount = articleService.getArticlesWithDiscountOn(date);
                List<BaseArticle> baseArticles = new ArrayList<>(articlesWithDiscount);
                return ResponseEntity.ok(baseArticles);
            }
        } else {
            List<Article> articles = articleService.getAllArticles();
            List<BaseArticle> baseArticles = new ArrayList<>(articles);
            return ResponseEntity.ok(baseArticles);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Article> getArticleById(@PathVariable Long id) {
        return articleService.getArticleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Article> updateArticle(@PathVariable Long id, @RequestBody Article article) {
        return articleService.updateArticle(id, article)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        if (articleService.deleteArticle(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.HEAD)
    public ResponseEntity<Void> articleExists(@PathVariable Long id) {
        if (articleService.existsById(id)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
