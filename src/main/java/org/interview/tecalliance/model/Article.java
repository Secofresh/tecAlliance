package org.interview.tecalliance.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class Article extends BaseArticle {

    private List<Discount> discounts = new ArrayList<>();

    public Article(Long id, String name, String slogan, BigDecimal netPrice,
                   BigDecimal salesPrice, BigDecimal vatRatio) {
        super(id, name, slogan, netPrice, salesPrice, vatRatio);
        this.discounts = new ArrayList<>();
    }

    public void addDiscount(Discount discount) {
        this.discounts.add(discount);
    }

    public void removeDiscount(Discount discount) {
        this.discounts.remove(discount);
    }

    public BigDecimal calculateDiscountedPrice(LocalDate date) {
        if (salesPrice == null || discounts == null || discounts.isEmpty()) {
            return salesPrice;
        }

        Discount applicableDiscount = discounts.stream()
                .filter(discount -> discount.isValidOn(date))
                .findFirst()
                .orElse(null);

        if (applicableDiscount == null) {
            return salesPrice;
        }

        BigDecimal discountAmount = salesPrice.multiply(applicableDiscount.getDiscountPercentage())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal discountedPrice = salesPrice.subtract(discountAmount);

        if (netPrice != null && discountedPrice.compareTo(netPrice) < 0) {
            return netPrice;
        }

        return discountedPrice;
    }

    public Discount getApplicableDiscount(LocalDate date) {
        if (discounts == null || discounts.isEmpty()) {
            return null;
        }
        return discounts.stream()
                .filter(discount -> discount.isValidOn(date))
                .findFirst()
                .orElse(null);
    }

    public boolean validateNoOverlappingDiscounts() {
        if (discounts == null || discounts.size() <= 1) {
            return true;
        }

        List<Discount> validDiscounts = discounts.stream()
                .filter(d -> d.getStartDate() != null && d.getEndDate() != null)
                .toList();

        // Check each pair of discounts for overlap
        for (int i = 0; i < validDiscounts.size(); i++) {
            Discount discount1 = validDiscounts.get(i);

            boolean hasOverlap = validDiscounts.stream()
                    .skip(i + 1L)
                    .anyMatch(discount2 -> doPeriodsOverlap(
                            discount1.getStartDate(),
                            discount1.getEndDate(),
                            discount2.getStartDate(),
                            discount2.getEndDate()));

            if (hasOverlap) {
                return false;
            }
        }
        return true;
    }

    private boolean doPeriodsOverlap(LocalDate start1, LocalDate end1, LocalDate start2, LocalDate end2) {
        return !start1.isAfter(end2) && !start2.isAfter(end1);
    }

    public boolean validateDiscounts() {
        if (salesPrice == null || netPrice == null) {
            return true;
        }
        if (!validateNoOverlappingDiscounts()) {
            return false;
        }
        if (discounts == null || discounts.isEmpty()) {
            return true;
        }
        return discounts.stream()
                .filter(discount -> discount.getDiscountPercentage() != null)
                .allMatch(discount -> {
                    BigDecimal discountAmount = salesPrice
                            .multiply(discount.getDiscountPercentage())
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                    BigDecimal discountedPrice = salesPrice.subtract(discountAmount);
                    return discountedPrice.compareTo(netPrice) >= 0;
                });
    }
}
