package org.interview.tecalliance.domain.model.article;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.interview.tecalliance.domain.model.Discount;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ArticleWithPrice extends BaseArticle {

    private BigDecimal finalPrice;
    private Discount appliedDiscount;
    private boolean hasActiveDiscount;

    public ArticleWithPrice(Long id, String name, String slogan, BigDecimal netPrice,
                            BigDecimal salesPrice, BigDecimal vatRatio, BigDecimal finalPrice,
                            Discount appliedDiscount) {
        super(id, name, slogan, netPrice, salesPrice, vatRatio);
        this.finalPrice = finalPrice;
        this.appliedDiscount = appliedDiscount;
        this.hasActiveDiscount = appliedDiscount != null;
    }

    public static ArticleWithPrice from(Article article, BigDecimal finalPrice, Discount appliedDiscount) {
        return new ArticleWithPrice(
                article.getId(),
                article.getName(),
                article.getSlogan(),
                article.getNetPrice(),
                article.getSalesPrice(),
                article.getVatRatio(),
                finalPrice,
                appliedDiscount
        );
    }
}
