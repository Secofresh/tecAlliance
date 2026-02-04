package org.interview.tecalliance.model;

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
        // Result: 120 - 30 = 90 < 100 (LOSS)
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("120.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Too High", new BigDecimal("25"),
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        article.addDiscount(discount);

        assertFalse(article.validateDiscounts());
    }

    @Test
    void testValidateDiscounts_DiscountAtExactNetPrice_ReturnsTrue() {
        // Result: 200 - 100 = 100 = net price (OK)
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
        // Discounts that share a boundary date (Jan 31)
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
    void testCalculateDiscountedPrice_WithActiveDiscount() {
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Summer Sale", new BigDecimal("30"),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        article.addDiscount(discount);

        LocalDate activeDate = LocalDate.of(2026, 6, 15);
        BigDecimal discountedPrice = article.calculateDiscountedPrice(activeDate);

        // 200 - (200 * 0.30) = 140
        assertEquals(new BigDecimal("140.00"), discountedPrice);
    }

    @Test
    void testCalculateDiscountedPrice_WithNoActiveDiscount() {
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount = new Discount(null, "Summer Sale", new BigDecimal("30"),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        article.addDiscount(discount);

        LocalDate inactiveDate = LocalDate.of(2026, 7, 15);
        BigDecimal discountedPrice = article.calculateDiscountedPrice(inactiveDate);

        // Should return sales price (no active discount)
        assertEquals(new BigDecimal("200.00"), discountedPrice);
    }

    @Test
    void testCalculateDiscountedPrice_CappedAtNetPrice() {
        //High discount that would go below net price
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("120.00"), new BigDecimal("0.19"));

        // This discount would result in: 120 - (120 * 0.30) = 84
        // But it should be capped at net price (100)
        Discount discount = new Discount(null, "Big Sale", new BigDecimal("30"),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));
        article.addDiscount(discount);

        LocalDate activeDate = LocalDate.of(2026, 6, 15);
        BigDecimal discountedPrice = article.calculateDiscountedPrice(activeDate);

        // Should be capped at net price (100), not 84
        assertEquals(new BigDecimal("100.00"), discountedPrice);
    }

    @Test
    void testValidateDiscounts_MultipleNonOverlappingDiscountsAllValid() {
        // Three non-overlapping discounts, all valid
        Article article = new Article(null, "Product", "Description",
                new BigDecimal("100.00"), new BigDecimal("200.00"), new BigDecimal("0.19"));

        Discount discount1 = new Discount(null, "Spring Sale", new BigDecimal("20"),
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31));

        Discount discount2 = new Discount(null, "Summer Sale", new BigDecimal("25"),
                LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30));

        Discount discount3 = new Discount(null, "Fall Sale", new BigDecimal("30"),
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 30));

        article.addDiscount(discount1);
        article.addDiscount(discount2);
        article.addDiscount(discount3);

        assertTrue(article.validateNoOverlappingDiscounts());
        assertTrue(article.validateDiscounts());
    }
}
