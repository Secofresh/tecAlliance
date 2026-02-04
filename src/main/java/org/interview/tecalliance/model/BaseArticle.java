package org.interview.tecalliance.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public abstract class BaseArticle {

    protected Long id;
    protected String name;
    protected String slogan;
    protected BigDecimal netPrice;
    protected BigDecimal salesPrice;
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
