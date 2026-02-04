package org.interview.tecalliance.service;

import org.interview.tecalliance.model.Article;
import org.interview.tecalliance.model.Discount;
import org.interview.tecalliance.repository.ArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ArticleService, focusing on discount validation.
 */
class ArticleServiceTest {

    @Mock
    private ArticleRepository articleRepository;

    @InjectMocks
    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateArticle_WithValidDiscounts_ShouldSucceed() {
        // Article with net price 100, sales price 200, discount 30%
        // Discounted price = 200 - (200 * 0.30) = 140, which is >= 100 (net price)
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Summer Sale", new BigDecimal("30"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        article.addDiscount(discount);

        when(articleRepository.save(any(Article.class))).thenReturn(article);

        Article savedArticle = articleService.createArticle(article);

        assertNotNull(savedArticle);
        verify(articleRepository, times(1)).save(article);
    }

    @Test
    void testCreateArticle_WithDiscountsCausingLoss_ShouldThrowException() {
        // Article with net price 100, sales price 120, discount 25%
        // Discounted price = 120 - (120 * 0.25) = 90, which is < 100 (net price) - LOSS!
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("120.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Too High Discount", new BigDecimal("25"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        article.addDiscount(discount);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            articleService.createArticle(article);
        });

        assertTrue(exception.getMessage().contains("Discounts would cause the article price to go below net price"));
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void testCreateArticle_WithSingleDiscountCausingLoss_ShouldThrowException() {
        // Article with net price 100, sales price 150
        // Single discount: 45%
        // Discounted price = 150 - (150 * 0.45) = 82.50, which is < 100 (net price) - LOSS!
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("150.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Too High Discount", new BigDecimal("45"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(7));

        article.addDiscount(discount);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            articleService.createArticle(article);
        });

        assertTrue(exception.getMessage().contains("Discounts would cause the article price to go below net price"));
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void testCreateArticle_WithDiscountsAtExactNetPrice_ShouldSucceed() {
        // Article with net price 100, sales price 200, discount 50%
        // Discounted price = 200 - (200 * 0.50) = 100, which is == 100 (net price) - OK!
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Half Price Sale", new BigDecimal("50"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        article.addDiscount(discount);

        when(articleRepository.save(any(Article.class))).thenReturn(article);

        Article savedArticle = articleService.createArticle(article);

        assertNotNull(savedArticle);
        verify(articleRepository, times(1)).save(article);
    }

    @Test
    void testUpdateArticle_WithDiscountsCausingLoss_ShouldThrowException() {
        Article existingArticle = new Article(1L, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("150.00"), new BigDecimal("0.19"));

        Article updatedArticle = new Article(1L, "Updated Product", "Updated Slogan",
                new BigDecimal("100.00"), new BigDecimal("120.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Too High Discount", new BigDecimal("25"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        updatedArticle.addDiscount(discount);

        when(articleRepository.findById(1L)).thenReturn(Optional.of(existingArticle));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            articleService.updateArticle(1L, updatedArticle);
        });

        assertTrue(exception.getMessage().contains("Discounts would cause the article price to go below net price"));
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void testCreateArticle_WithNoDiscounts_ShouldSucceed() {
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        when(articleRepository.save(any(Article.class))).thenReturn(article);

        Article savedArticle = articleService.createArticle(article);

        assertNotNull(savedArticle);
        verify(articleRepository, times(1)).save(article);
    }

    @Test
    void testCreateArticle_WithOverlappingDiscounts_ShouldThrowException() {
        // Two discounts with overlapping date ranges
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        // Discount 1: Jan 1 to Jan 31
        Discount discount1 = new Discount(null, "January Sale", new BigDecimal("10"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        // Discount 2: Jan 15 to Feb 15 (overlaps with discount 1)
        Discount discount2 = new Discount(null, "Winter Sale", new BigDecimal("15"),
                LocalDate.of(2026, 1, 15), LocalDate.of(2026, 2, 15));

        article.addDiscount(discount1);
        article.addDiscount(discount2);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            articleService.createArticle(article);
        });

        assertTrue(exception.getMessage().contains("overlapping date ranges"));
        assertTrue(exception.getMessage().contains("Only one discount can be applicable at a time"));
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void testCreateArticle_WithNonOverlappingDiscounts_ShouldSucceed() {
        // Two discounts with non-overlapping date ranges
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        // Discount 1: Jan 1 to Jan 31
        Discount discount1 = new Discount(null, "January Sale", new BigDecimal("20"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        // Discount 2: Feb 1 to Feb 28 (no overlap)
        Discount discount2 = new Discount(null, "February Sale", new BigDecimal("15"),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));

        article.addDiscount(discount1);
        article.addDiscount(discount2);

        when(articleRepository.save(any(Article.class))).thenReturn(article);

        Article savedArticle = articleService.createArticle(article);

        assertNotNull(savedArticle);
        verify(articleRepository, times(1)).save(article);
    }

    @Test
    void testCreateArticle_WithAdjacentDiscounts_ShouldThrowException() {
        // Two discounts where end date of one equals start date of another (still overlaps)
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        // Discount 1: Jan 1 to Jan 31
        Discount discount1 = new Discount(null, "January Sale", new BigDecimal("20"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        // Discount 2: Jan 31 to Feb 15 (overlaps on Jan 31)
        Discount discount2 = new Discount(null, "February Sale", new BigDecimal("15"),
                LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 15));

        article.addDiscount(discount1);
        article.addDiscount(discount2);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            articleService.createArticle(article);
        });

        assertTrue(exception.getMessage().contains("overlapping date ranges"));
        verify(articleRepository, never()).save(any(Article.class));
    }

    @Test
    void testUpdateArticle_WithOverlappingDiscounts_ShouldThrowException() {
        Article existingArticle = new Article(1L, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("150.00"), new BigDecimal("0.19"));

        Article updatedArticle = new Article(1L, "Updated Product", "Updated Slogan",
                new BigDecimal("100.00"), new BigDecimal("150.00"), new BigDecimal("0.19"));

        // Add overlapping discounts
        Discount discount1 = new Discount(null, "Summer Sale", new BigDecimal("10"),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        Discount discount2 = new Discount(null, "Mid-year Sale", new BigDecimal("15"),
                LocalDate.of(2026, 6, 15), LocalDate.of(2026, 7, 15));

        updatedArticle.addDiscount(discount1);
        updatedArticle.addDiscount(discount2);

        when(articleRepository.findById(1L)).thenReturn(Optional.of(existingArticle));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            articleService.updateArticle(1L, updatedArticle);
        });

        assertTrue(exception.getMessage().contains("overlapping date ranges"));
        verify(articleRepository, never()).save(any(Article.class));
    }
}
