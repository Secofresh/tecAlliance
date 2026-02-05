package org.interview.tecalliance.domain.model;

import org.interview.tecalliance.domain.model.article.Article;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ArticleDiscountValidationTest {

    @Test
    void testValidateDiscounts_SingleValidDiscount_ReturnsTrue() {
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Sale", new BigDecimal("30"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        article.addDiscount(discount);

        assertTrue(article.validateDiscounts());
        assertTrue(article.validateNoOverlappingDiscounts());
    }

    @Test
    void testValidateDiscounts_DiscountCausingLoss_ReturnsFalse() {
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("120.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Too High", new BigDecimal("25"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        article.addDiscount(discount);

        assertFalse(article.validateDiscounts());
    }

    @Test
    void testValidateDiscounts_DiscountAtExactNetPrice_ReturnsTrue() {
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Half Off", new BigDecimal("50"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        article.addDiscount(discount);

        assertTrue(article.validateDiscounts());
    }

    @Test
    void testValidateNoOverlappingDiscounts_OverlappingPeriods_ReturnsFalse() {
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount1 = new Discount(null, "Jan Sale", new BigDecimal("10"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        Discount discount2 = new Discount(null, "Winter Sale", new BigDecimal("15"),
                LocalDate.of(2026, 1, 15), LocalDate.of(2026, 2, 15));

        article.addDiscount(discount1);
        article.addDiscount(discount2);

        assertFalse(article.validateNoOverlappingDiscounts());
    }

    @Test
    void testValidateNoOverlappingDiscounts_NonOverlappingPeriods_ReturnsTrue() {
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount1 = new Discount(null, "Jan Sale", new BigDecimal("20"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        Discount discount2 = new Discount(null, "Feb Sale", new BigDecimal("15"),
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));

        article.addDiscount(discount1);
        article.addDiscount(discount2);

        assertTrue(article.validateNoOverlappingDiscounts());
    }

    @Test
    void testValidateNoOverlappingDiscounts_AdjacentDates_ReturnsFalse() {
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount1 = new Discount(null, "Jan Sale", new BigDecimal("20"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));

        Discount discount2 = new Discount(null, "Feb Sale", new BigDecimal("15"),
                LocalDate.of(2026, 1, 31), LocalDate.of(2026, 2, 28));

        article.addDiscount(discount1);
        article.addDiscount(discount2);

        assertFalse(article.validateNoOverlappingDiscounts());
    }

    @Test
    void testCalculateDiscountedPrice_WithValidDiscount_CalculatesCorrectly() {
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Sale", new BigDecimal("25"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        article.addDiscount(discount);

        BigDecimal discountedPrice = article.calculateDiscountedPrice(LocalDate.of(2026, 1, 15));
        assertEquals(new BigDecimal("150.00"), discountedPrice);
    }

    @Test
    void testCalculateDiscountedPrice_NoActiveDiscount_ReturnsSalesPrice() {
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Sale", new BigDecimal("25"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        article.addDiscount(discount);

        BigDecimal price = article.calculateDiscountedPrice(LocalDate.of(2026, 2, 15));
        assertEquals(new BigDecimal("200.00"), price);
    }

    @Test
    void testGetApplicableDiscount_WithActiveDiscount_ReturnsDiscount() {
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Sale", new BigDecimal("25"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        article.addDiscount(discount);

        Discount applied = article.getApplicableDiscount(LocalDate.of(2026, 1, 15));
        assertNotNull(applied);
        assertEquals("Sale", applied.getDescription());
    }

    @Test
    void testGetApplicableDiscount_NoActiveDiscount_ReturnsNull() {
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Sale", new BigDecimal("25"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        article.addDiscount(discount);

        Discount applied = article.getApplicableDiscount(LocalDate.of(2026, 2, 15));
        assertNull(applied);
    }
}
