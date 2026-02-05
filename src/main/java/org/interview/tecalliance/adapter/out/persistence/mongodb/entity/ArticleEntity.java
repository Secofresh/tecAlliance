package org.interview.tecalliance.adapter.out.persistence.mongodb.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.interview.tecalliance.domain.model.Discount;
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
    private ObjectId id;

    private String name;
    private String slogan;
    private BigDecimal netPrice;
    private BigDecimal salesPrice;
    private BigDecimal vatRatio;
    private List<Discount> discounts = new ArrayList<>();

}
