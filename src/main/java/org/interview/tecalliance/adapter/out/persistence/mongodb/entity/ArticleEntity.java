package org.interview.tecalliance.adapter.out.persistence.mongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.interview.tecalliance.domain.model.Discount;
import org.interview.tecalliance.domain.model.article.Article;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "articles")
public class ArticleEntity {

    @Id
    private String id;

    private String name;
    private String slogan;
    private BigDecimal netPrice;
    private BigDecimal salesPrice;
    private BigDecimal vatRatio;
    private List<Discount> discounts = new ArrayList<>();

    /**
     * Convert domain Article to MongoDB entity
     */
    public static ArticleEntity fromDomain(Article article) {
        ArticleEntity entity = new ArticleEntity();
        if (article.getId() != null) {
            entity.setId(article.getId().toString());
        }
        entity.setName(article.getName());
        entity.setSlogan(article.getSlogan());
        entity.setNetPrice(article.getNetPrice());
        entity.setSalesPrice(article.getSalesPrice());
        entity.setVatRatio(article.getVatRatio());
        entity.setDiscounts(article.getDiscounts() != null ? article.getDiscounts() : new ArrayList<>());
        return entity;
    }

    /**
     * Convert MongoDB entity to domain Article
     */
    public Article toDomain() {
        Article article = new Article();
        if (this.id != null) {
            article.setId(Long.parseLong(this.id));
        }
        article.setName(this.name);
        article.setSlogan(this.slogan);
        article.setNetPrice(this.netPrice);
        article.setSalesPrice(this.salesPrice);
        article.setVatRatio(this.vatRatio);
        article.setDiscounts(this.discounts != null ? this.discounts : new ArrayList<>());
        return article;
    }
}
