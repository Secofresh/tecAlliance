package org.interview.tecalliance.domain.model.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Schema(description = "Base article information")
public abstract class BaseArticle {

    @Schema(description = "Unique identifier of the article", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    protected Long id;

    @Schema(description = "Name of the article", example = "Laptop Pro 15", requiredMode = Schema.RequiredMode.REQUIRED)
    protected String name;

    @Schema(description = "Marketing slogan for the article", example = "Best laptop ever!")
    protected String slogan;

    @Schema(description = "Net price (minimum price, before VAT)", example = "500.00", requiredMode = Schema.RequiredMode.REQUIRED)
    protected BigDecimal netPrice;

    @Schema(description = "Sales price (regular price, before VAT)", example = "800.00", requiredMode = Schema.RequiredMode.REQUIRED)
    protected BigDecimal salesPrice;

    @Schema(description = "VAT ratio (e.g., 0.19 for 19%)", example = "0.19", requiredMode = Schema.RequiredMode.REQUIRED)
    protected BigDecimal vatRatio;

    protected BaseArticle(Long id, String name, String slogan,
                         BigDecimal netPrice, BigDecimal salesPrice, BigDecimal vatRatio) {
        this.id = id;
        this.name = name;
        this.slogan = slogan;
        this.netPrice = netPrice;
        this.salesPrice = salesPrice;
        this.vatRatio = vatRatio;
    }
}
