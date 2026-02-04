package org.interview.tecalliance.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.interview.tecalliance.model.Article;
import org.interview.tecalliance.model.BaseArticle;
import org.interview.tecalliance.model.Discount;

import java.math.BigDecimal;

/**
 * DTO for Article with calculated final price after applying discounts.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ArticleWithPriceDTO extends BaseArticle {

    private BigDecimal finalPrice;
    private Discount appliedDiscount;
    private boolean hasActiveDiscount;

    public static ArticleWithPriceDTO from(Article article, BigDecimal finalPrice, Discount appliedDiscount) {
        ArticleWithPriceDTO dto = new ArticleWithPriceDTO();
        dto.setId(article.getId());
        dto.setName(article.getName());
        dto.setSlogan(article.getSlogan());
        dto.setNetPrice(article.getNetPrice());
        dto.setSalesPrice(article.getSalesPrice());
        dto.setFinalPrice(finalPrice);
        dto.setVatRatio(article.getVatRatio());
        dto.setAppliedDiscount(appliedDiscount);
        dto.setHasActiveDiscount(appliedDiscount != null);
        return dto;
    }
}
