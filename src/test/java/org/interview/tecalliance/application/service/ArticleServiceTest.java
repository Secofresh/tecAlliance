package org.interview.tecalliance.application.service;

import org.interview.tecalliance.application.port.out.ArticlePersistencePort;
import org.interview.tecalliance.domain.model.article.Article;
import org.interview.tecalliance.domain.model.Discount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @Mock
    private ArticlePersistencePort persistencePort;

    @InjectMocks
    private ArticleService articleService;

    @Test
    void testCreateArticle_WithValidDiscounts_ShouldSucceed() {
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Summer Sale", new BigDecimal("30"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        article.addDiscount(discount);

        when(persistencePort.save(any(Article.class))).thenReturn(article);

        Article savedArticle = articleService.createArticle(article);

        assertNotNull(savedArticle);
        verify(persistencePort, times(1)).save(article);
    }

    @Test
    void testCreateArticle_WithDiscountsCausingLoss_ShouldThrowException() {
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("120.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Too High Discount", new BigDecimal("25"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        article.addDiscount(discount);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> articleService.createArticle(article));

        assertTrue(exception.getMessage().contains("Discounts would cause the article price to go below net price"));
        verify(persistencePort, never()).save(any(Article.class));
    }

    @Test
    void testCreateArticle_WithSingleDiscountCausingLoss_ShouldThrowException() {
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("150.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Too High Discount", new BigDecimal("45"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(7));

        article.addDiscount(discount);

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> articleService.createArticle(article));

        assertTrue(exception.getMessage().contains("Discounts would cause the article price to go below net price"));
        verify(persistencePort, never()).save(any(Article.class));
    }

    @Test
    void testCreateArticle_WithDiscountsAtExactNetPrice_ShouldSucceed() {
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Half Price Sale", new BigDecimal("50"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        article.addDiscount(discount);

        when(persistencePort.save(any(Article.class))).thenReturn(article);

        Article savedArticle = articleService.createArticle(article);

        assertNotNull(savedArticle);
        verify(persistencePort, times(1)).save(article);
    }

    @Test
    void testUpdateArticle_WithDiscountsCausingLoss_ShouldThrowException() {
        Article existingArticle = new Article("1", "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("150.00"), new BigDecimal("0.19"));

        Article updatedArticle = new Article("1", "Updated Product", "Updated Slogan",
                new BigDecimal("100.00"), new BigDecimal("120.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Too High Discount", new BigDecimal("25"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        updatedArticle.addDiscount(discount);

        when(persistencePort.findById("1")).thenReturn(Optional.of(existingArticle));

        IllegalArgumentException exception =
                assertThrows(IllegalArgumentException.class, () -> articleService.updateArticle("1", updatedArticle));

        assertTrue(exception.getMessage().contains("Discounts would cause the article price to go below net price"));
        verify(persistencePort, never()).save(any(Article.class));
    }

    @Test
    void testCreateArticle_WithNoDiscounts_ShouldSucceed() {
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        when(persistencePort.save(any(Article.class))).thenReturn(article);

        Article savedArticle = articleService.createArticle(article);

        assertNotNull(savedArticle);
        verify(persistencePort, times(1)).save(article);
    }

    @Test
    void testCreateArticle_WithOverlappingDiscounts_ShouldThrowException() {
        Article article = new Article(null, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount1 = new Discount(null, "Summer Sale", new BigDecimal("20"),
                LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30));
        Discount discount2 = new Discount(null, "Mid-Year Sale", new BigDecimal("25"),
                LocalDate.of(2024, 6, 15), LocalDate.of(2024, 7, 15));

        article.addDiscount(discount1);
        article.addDiscount(discount2);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            articleService.createArticle(article);
        });

        assertTrue(exception.getMessage().contains("overlapping"));
        verify(persistencePort, never()).save(any(Article.class));
    }
}
