package org.interview.tecalliance.application.service;

import org.interview.tecalliance.application.port.in.ArticleUseCase;
import org.interview.tecalliance.application.port.out.ArticlePersistencePort;
import org.interview.tecalliance.domain.model.article.Article;
import org.interview.tecalliance.domain.model.article.ArticleWithPrice;
import org.interview.tecalliance.domain.model.Discount;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Application Service that implements Article use cases.
 * This is the core business logic layer independent of frameworks.
 */
public class ArticleService implements ArticleUseCase {

    private final ArticlePersistencePort persistencePort;

    public ArticleService(ArticlePersistencePort persistencePort) {
        this.persistencePort = persistencePort;
    }

    @Override
    public Article createArticle(Article article) {
        article.setId(null);
        validateArticle(article);
        return persistencePort.save(article);
    }

    @Override
    public Optional<Article> getArticleById(Long id) {
        return persistencePort.findById(id);
    }

    @Override
    public List<Article> getAllArticles() {
        return persistencePort.findAll();
    }

    @Override
    public List<ArticleWithPrice> getArticlesWithPrices(LocalDate date) {
        return persistencePort.findAll().stream()
                .map(article -> {
                    BigDecimal finalPrice = article.calculateDiscountedPrice(date);
                    Discount appliedDiscount = article.getApplicableDiscount(date);
                    return ArticleWithPrice.from(article, finalPrice, appliedDiscount);
                })
                .toList();
    }

    @Override
    public List<Article> getArticlesWithDiscountOn(LocalDate date) {
        return persistencePort.findAll().stream()
                .filter(article -> article.getDiscounts() != null &&
                        article.getDiscounts().stream()
                                .anyMatch(discount -> discount.isValidOn(date)))
                .toList();
    }

    @Override
    public Optional<Article> updateArticle(Long id, Article updatedArticle) {
        return persistencePort.findById(id).map(existingArticle -> {
            existingArticle.setName(updatedArticle.getName());
            existingArticle.setSlogan(updatedArticle.getSlogan());
            existingArticle.setNetPrice(updatedArticle.getNetPrice());
            existingArticle.setSalesPrice(updatedArticle.getSalesPrice());
            existingArticle.setVatRatio(updatedArticle.getVatRatio());
            if (updatedArticle.getDiscounts() != null) {
                existingArticle.setDiscounts(updatedArticle.getDiscounts());
            }
            validateArticle(existingArticle);
            return persistencePort.save(existingArticle);
        });
    }

    @Override
    public boolean deleteArticle(Long id) {
        return persistencePort.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return persistencePort.existsById(id);
    }

    private void validateArticle(Article article) {
        if (!article.validateNoOverlappingDiscounts()) {
            throw new IllegalArgumentException(
                "Multiple discounts have overlapping date ranges. Only one discount can be applicable at a time.");
        }
        if (!article.validateDiscounts()) {
            throw new IllegalArgumentException(
                "Discounts would cause the article price to go below net price, resulting in a loss");
        }
    }
}
