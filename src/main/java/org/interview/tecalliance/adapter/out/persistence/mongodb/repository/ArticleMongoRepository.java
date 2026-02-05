package org.interview.tecalliance.adapter.out.persistence.mongodb.repository;

import org.bson.types.ObjectId;
import org.interview.tecalliance.adapter.out.persistence.mongodb.entity.ArticleEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleMongoRepository extends MongoRepository<ArticleEntity, ObjectId> {
}
