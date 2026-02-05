package org.interview.tecalliance.adapter.out.persistence.mongodb.mapper;

import org.bson.types.ObjectId;
import org.interview.tecalliance.adapter.out.persistence.mongodb.entity.ArticleEntity;
import org.interview.tecalliance.domain.model.article.Article;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface ArticleEntityMapper {

    @Mapping(target = "id", source = "id", qualifiedByName = "stringToObjectId")
    ArticleEntity toEntity(Article article);

    @Mapping(target = "id", source = "id", qualifiedByName = "objectIdToString")
    Article toDomain(ArticleEntity entity);

    @Named("stringToObjectId")
    default ObjectId stringToObjectId(String id) {
        if (id == null || id.isEmpty()) {
            return null;
        }
        return new ObjectId(id);
    }

    @Named("objectIdToString")
    default String objectIdToString(ObjectId id) {
        return id != null ? id.toHexString() : null;
    }
}
