package org.interview.tecalliance.service;

import org.interview.tecalliance.dto.ArticleWithPriceDTO;
import org.interview.tecalliance.model.Article;
import org.interview.tecalliance.model.Discount;
import org.interview.tecalliance.repository.ArticleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    public ArticleService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public Article createArticle(Article article) {
        article.setId(null);
        if (!article.validateNoOverlappingDiscounts()) {
            throw new IllegalArgumentException(
                "Multiple discounts have overlapping date ranges. Only one discount can be applicable at a time.");
        }
        if (!article.validateDiscounts()) {
            throw new IllegalArgumentException(
                "Discounts would cause the article price to go below net price, resulting in a loss");
        }
        return articleRepository.save(article);
    }

    public Optional<Article> getArticleById(Long id) {
        return articleRepository.findById(id);
    }

    public List<Article> getAllArticles() {
        return articleRepository.findAll();
    }

    public List<ArticleWithPriceDTO> getArticlesWithPrices(LocalDate date) {
        return articleRepository.findAll().stream()
                .map(article -> {
                    BigDecimal finalPrice = article.calculateDiscountedPrice(date);
                    Discount appliedDiscount = article.getApplicableDiscount(date);
                    return ArticleWithPriceDTO.from(article, finalPrice, appliedDiscount);
                })
                .toList();
    }

    public List<Article> getArticlesWithDiscountOn(LocalDate date) {
        return articleRepository.findAll().stream()
                .filter(article -> article.getDiscounts() != null &&
                        article.getDiscounts().stream()
                                .anyMatch(discount -> discount.isValidOn(date)))
                .toList();
    }

    public Optional<Article> updateArticle(Long id, Article updatedArticle) {
        return articleRepository.findById(id).map(existingArticle -> {
            existingArticle.setName(updatedArticle.getName());
            existingArticle.setSlogan(updatedArticle.getSlogan());
            existingArticle.setNetPrice(updatedArticle.getNetPrice());
            existingArticle.setSalesPrice(updatedArticle.getSalesPrice());
            existingArticle.setVatRatio(updatedArticle.getVatRatio());
            if (updatedArticle.getDiscounts() != null) {
                existingArticle.setDiscounts(updatedArticle.getDiscounts());
            }
            if (!existingArticle.validateNoOverlappingDiscounts()) {
                throw new IllegalArgumentException(
                    "Multiple discounts have overlapping date ranges. Only one discount can be applicable at a time.");
            }
            if (!existingArticle.validateDiscounts()) {
                throw new IllegalArgumentException(
                    "Discounts would cause the article price to go below net price, resulting in a loss");
            }
            return articleRepository.save(existingArticle);
        });
    }

    public boolean deleteArticle(Long id) {
        return articleRepository.deleteById(id);
    }

    public boolean existsById(Long id) {
        return articleRepository.existsById(id);
    }
}
