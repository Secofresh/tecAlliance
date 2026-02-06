package org.interview.tecalliance.application.service;

import org.interview.tecalliance.application.port.out.ArticlePersistencePort;
import org.interview.tecalliance.domain.model.article.Article;
import org.interview.tecalliance.domain.model.article.ArticleWithPrice;
import org.interview.tecalliance.domain.model.article.BaseArticle;
import org.interview.tecalliance.domain.model.Discount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> articleService.createArticle(article));

        assertTrue(exception.getMessage().contains("overlapping"));
        verify(persistencePort, never()).save(any(Article.class));
    }

    @Test
    void testGetArticleById_WhenArticleExists_ShouldReturnArticle() {
        String articleId = "123";
        Article article = new Article(articleId, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        when(persistencePort.findById(articleId)).thenReturn(Optional.of(article));

        Optional<Article> result = articleService.getArticleById(articleId);

        assertTrue(result.isPresent());
        assertEquals(articleId, result.get().getId());
        assertEquals("Test Product", result.get().getName());
        verify(persistencePort, times(1)).findById(articleId);
    }

    @Test
    void testGetArticleById_WhenArticleDoesNotExist_ShouldReturnEmpty() {
        String articleId = "nonexistent";

        when(persistencePort.findById(articleId)).thenReturn(Optional.empty());

        Optional<Article> result = articleService.getArticleById(articleId);

        assertFalse(result.isPresent());
        verify(persistencePort, times(1)).findById(articleId);
    }

    @Test
    void testGetAllArticles_WhenArticlesExist_ShouldReturnAllArticles() {
        Article article1 = new Article("1", "Product 1", "Slogan 1",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));
        Article article2 = new Article("2", "Product 2", "Slogan 2",
                new BigDecimal("150.00"), new BigDecimal("250.00"), new BigDecimal("0.19"));

        List<Article> articles = Arrays.asList(article1, article2);
        when(persistencePort.findAll()).thenReturn(articles);

        List<Article> result = articleService.getAllArticles();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Product 1", result.get(0).getName());
        assertEquals("Product 2", result.get(1).getName());
        verify(persistencePort, times(1)).findAll();
    }

    @Test
    void testGetAllArticles_WhenNoArticles_ShouldReturnEmptyList() {
        when(persistencePort.findAll()).thenReturn(Collections.emptyList());

        List<Article> result = articleService.getAllArticles();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(persistencePort, times(1)).findAll();
    }

    @Test
    void testGetArticlesWithFilters_WithNoFilters_ShouldReturnAllArticles() {
        Article article1 = new Article("1", "Product 1", "Slogan 1",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));
        Article article2 = new Article("2", "Product 2", "Slogan 2",
                new BigDecimal("150.00"), new BigDecimal("250.00"), new BigDecimal("0.19"));

        when(persistencePort.findAll()).thenReturn(Arrays.asList(article1, article2));

        List<BaseArticle> result = articleService.getArticlesWithFilters(null, false, false);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertInstanceOf(Article.class, result.getFirst());
        verify(persistencePort, times(1)).findAll();
    }

    @Test
    void testGetArticlesWithFilters_WithPrices_ShouldReturnArticlesWithPrices() {
        LocalDate testDate = LocalDate.of(2026, 2, 6);
        Article article = new Article("1", "Product 1", "Slogan 1",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Sale", new BigDecimal("10"),
                testDate.minusDays(5), testDate.plusDays(5));
        article.addDiscount(discount);

        when(persistencePort.findAll()).thenReturn(Collections.singletonList(article));

        List<BaseArticle> result = articleService.getArticlesWithFilters(testDate, true, false);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertInstanceOf(ArticleWithPrice.class, result.getFirst());
        ArticleWithPrice articleWithPrice = (ArticleWithPrice) result.getFirst();
        assertNotNull(articleWithPrice.getFinalPrice());
        verify(persistencePort, times(1)).findAll();
    }

    @Test
    void testGetArticlesWithFilters_WithDiscountOnly_ShouldReturnOnlyArticlesWithActiveDiscounts() {
        LocalDate testDate = LocalDate.of(2026, 2, 6);

        Article articleWithDiscount = new Article("1", "Product 1", "Slogan 1",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));
        Discount activeDiscount = new Discount(null, "Sale", new BigDecimal("10"),
                testDate.minusDays(5), testDate.plusDays(5));
        articleWithDiscount.addDiscount(activeDiscount);

        Article articleWithoutDiscount = new Article("2", "Product 2", "Slogan 2",
                new BigDecimal("150.00"), new BigDecimal("250.00"), new BigDecimal("0.19"));

        when(persistencePort.findAll()).thenReturn(Arrays.asList(articleWithDiscount, articleWithoutDiscount));

        List<BaseArticle> result = articleService.getArticlesWithFilters(testDate, false, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertInstanceOf(Article.class, result.getFirst());
        assertEquals("1", result.getFirst().getId());
        verify(persistencePort, times(1)).findAll();
    }

    @Test
    void testGetArticlesWithFilters_WithPricesAndDiscountOnly_ShouldReturnArticlesWithPricesAndActiveDiscounts() {
        LocalDate testDate = LocalDate.of(2026, 2, 6);

        Article articleWithDiscount = new Article("1", "Product 1", "Slogan 1",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));
        Discount activeDiscount = new Discount(null, "Sale", new BigDecimal("10"),
                testDate.minusDays(5), testDate.plusDays(5));
        articleWithDiscount.addDiscount(activeDiscount);

        Article articleWithoutDiscount = new Article("2", "Product 2", "Slogan 2",
                new BigDecimal("150.00"), new BigDecimal("250.00"), new BigDecimal("0.19"));

        when(persistencePort.findAll()).thenReturn(Arrays.asList(articleWithDiscount, articleWithoutDiscount));

        List<BaseArticle> result = articleService.getArticlesWithFilters(testDate, true, true);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertInstanceOf(ArticleWithPrice.class, result.getFirst());
        ArticleWithPrice articleWithPrice = (ArticleWithPrice) result.getFirst();
        assertNotNull(articleWithPrice.getAppliedDiscount());
        verify(persistencePort, times(1)).findAll();
    }

    @Test
    void testGetArticlesWithFilters_WithNullDateAndPricesTrue_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> articleService.getArticlesWithFilters(null, true, false));

        assertTrue(exception.getMessage().contains("Date parameter is required"));
        verify(persistencePort, never()).findAll();
    }

    @Test
    void testGetArticlesWithFilters_WithNullDateAndDiscountOnlyTrue_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> articleService.getArticlesWithFilters(null, false, true));

        assertTrue(exception.getMessage().contains("Date parameter is required"));
        verify(persistencePort, never()).findAll();
    }

    @Test
    void testUpdateArticle_WithValidData_ShouldUpdateAndReturnArticle() {
        String articleId = "1";
        Article existingArticle = new Article(articleId, "Old Product", "Old Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Article updatedArticle = new Article(articleId, "Updated Product", "Updated Slogan",
                new BigDecimal("120.00"), new BigDecimal("220.00"), new BigDecimal("0.19"));

        when(persistencePort.findById(articleId)).thenReturn(Optional.of(existingArticle));
        when(persistencePort.save(any(Article.class))).thenReturn(existingArticle);

        Optional<Article> result = articleService.updateArticle(articleId, updatedArticle);

        assertTrue(result.isPresent());
        assertEquals("Updated Product", result.get().getName());
        assertEquals("Updated Slogan", result.get().getSlogan());
        verify(persistencePort, times(1)).findById(articleId);
        verify(persistencePort, times(1)).save(any(Article.class));
    }

    @Test
    void testUpdateArticle_WhenArticleNotFound_ShouldReturnEmpty() {
        String articleId = "nonexistent";
        Article updatedArticle = new Article(articleId, "Updated Product", "Updated Slogan",
                new BigDecimal("120.00"), new BigDecimal("220.00"), new BigDecimal("0.19"));

        when(persistencePort.findById(articleId)).thenReturn(Optional.empty());

        Optional<Article> result = articleService.updateArticle(articleId, updatedArticle);

        assertFalse(result.isPresent());
        verify(persistencePort, times(1)).findById(articleId);
        verify(persistencePort, never()).save(any(Article.class));
    }

    @Test
    void testUpdateArticle_WithValidDiscounts_ShouldSucceed() {
        String articleId = "1";
        Article existingArticle = new Article(articleId, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Article updatedArticle = new Article(articleId, "Updated Product", "Updated Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));
        Discount discount = new Discount(null, "Valid Sale", new BigDecimal("20"),
                LocalDate.now().minusDays(1), LocalDate.now().plusDays(30));
        updatedArticle.addDiscount(discount);

        when(persistencePort.findById(articleId)).thenReturn(Optional.of(existingArticle));
        when(persistencePort.save(any(Article.class))).thenReturn(existingArticle);

        Optional<Article> result = articleService.updateArticle(articleId, updatedArticle);

        assertTrue(result.isPresent());
        verify(persistencePort, times(1)).save(any(Article.class));
    }

    @Test
    void testUpdateArticle_WithOverlappingDiscounts_ShouldThrowException() {
        String articleId = "1";
        Article existingArticle = new Article(articleId, "Test Product", "Test Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Article updatedArticle = new Article(articleId, "Updated Product", "Updated Slogan",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount1 = new Discount(null, "Summer Sale", new BigDecimal("20"),
                LocalDate.of(2024, 6, 1), LocalDate.of(2024, 6, 30));
        Discount discount2 = new Discount(null, "Mid-Year Sale", new BigDecimal("25"),
                LocalDate.of(2024, 6, 15), LocalDate.of(2024, 7, 15));
        updatedArticle.addDiscount(discount1);
        updatedArticle.addDiscount(discount2);

        when(persistencePort.findById(articleId)).thenReturn(Optional.of(existingArticle));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class, () -> articleService.updateArticle(articleId, updatedArticle));

        assertTrue(exception.getMessage().contains("overlapping"));
        verify(persistencePort, never()).save(any(Article.class));
    }

    // Tests for deleteArticle
    @Test
    void testDeleteArticle_WhenArticleExists_ShouldReturnTrue() {
        String articleId = "1";
        when(persistencePort.deleteById(articleId)).thenReturn(true);

        boolean result = articleService.deleteArticle(articleId);

        assertTrue(result);
        verify(persistencePort, times(1)).deleteById(articleId);
    }

    @Test
    void testDeleteArticle_WhenArticleDoesNotExist_ShouldReturnFalse() {
        String articleId = "nonexistent";
        when(persistencePort.deleteById(articleId)).thenReturn(false);

        boolean result = articleService.deleteArticle(articleId);

        assertFalse(result);
        verify(persistencePort, times(1)).deleteById(articleId);
    }

    @Test
    void testExistsById_WhenArticleExists_ShouldReturnTrue() {
        String articleId = "1";
        when(persistencePort.existsById(articleId)).thenReturn(true);

        boolean result = articleService.existsById(articleId);

        assertTrue(result);
        verify(persistencePort, times(1)).existsById(articleId);
    }

    @Test
    void testExistsById_WhenArticleDoesNotExist_ShouldReturnFalse() {
        String articleId = "nonexistent";
        when(persistencePort.existsById(articleId)).thenReturn(false);

        boolean result = articleService.existsById(articleId);

        assertFalse(result);
        verify(persistencePort, times(1)).existsById(articleId);
    }
}
