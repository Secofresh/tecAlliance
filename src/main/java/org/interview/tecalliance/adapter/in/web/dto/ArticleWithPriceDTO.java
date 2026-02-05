package org.interview.tecalliance.adapter.in.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.interview.tecalliance.domain.model.article.ArticleWithPrice;
import org.interview.tecalliance.domain.model.article.BaseArticle;
import org.interview.tecalliance.domain.model.Discount;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Schema(description = "Article with calculated price information and active discount details")
public class ArticleWithPriceDTO extends BaseArticle {

    @Schema(description = "Final price after applying discounts (before VAT)", example = "680.00")
    private BigDecimal finalPrice;

    @Schema(description = "Discount currently applied to this article", nullable = true)
    private Discount appliedDiscount;

    @Schema(description = "Indicates whether an active discount is applied", example = "true")
    private boolean hasActiveDiscount;

    public static ArticleWithPriceDTO fromDomain(ArticleWithPrice domainObject) {
        ArticleWithPriceDTO dto = new ArticleWithPriceDTO();
        dto.setId(domainObject.getId());
        dto.setName(domainObject.getName());
        dto.setSlogan(domainObject.getSlogan());
        dto.setNetPrice(domainObject.getNetPrice());
        dto.setSalesPrice(domainObject.getSalesPrice());
        dto.setFinalPrice(domainObject.getFinalPrice());
        dto.setVatRatio(domainObject.getVatRatio());
        dto.setAppliedDiscount(domainObject.getAppliedDiscount());
        dto.setHasActiveDiscount(domainObject.isHasActiveDiscount());
        return dto;
    }
}
