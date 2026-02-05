package org.interview.tecalliance.adapter.out.persistence.mongodb;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.interview.tecalliance.adapter.out.persistence.mongodb.entity.ArticleEntity;
import org.interview.tecalliance.adapter.out.persistence.mongodb.mapper.ArticleEntityMapper;
import org.interview.tecalliance.adapter.out.persistence.mongodb.repository.ArticleMongoRepository;
import org.interview.tecalliance.application.port.out.ArticlePersistencePort;
import org.interview.tecalliance.domain.model.article.Article;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class MongoArticlePersistenceAdapter implements ArticlePersistencePort {

    private final ArticleMongoRepository repository;
    private final ArticleEntityMapper mapper;

    @Override
    public Article save(Article article) {
        log.debug("Saving article: {}", article.getName());

        if (article.getDiscounts() == null) {
            article.setDiscounts(new ArrayList<>());
        }

        ArticleEntity entity = mapper.toEntity(article);
        ArticleEntity saved = repository.save(entity);
        Article savedArticle = mapper.toDomain(saved);

        log.info("Article saved successfully with ID: {}", savedArticle.getId());
        return savedArticle;
    }

    @Override
    public Optional<Article> findById(String id) {
        log.debug("Finding article by ID: {}", id);
        if (id == null || id.isEmpty()) {
            log.error("Attempted to find article with null or empty ID");
            return Optional.empty();
        }

        try {
            ObjectId objectId = new ObjectId(id);
            Optional<Article> result = repository.findById(objectId)
                    .map(mapper::toDomain);

            if (result.isPresent()) {
                log.debug("Article found with ID: {}", id);
            } else {
                log.debug("Article not found with ID: {}", id);
            }
            return result;
        } catch (IllegalArgumentException _) {
            log.error("Invalid ObjectId format: {}", id);
            return Optional.empty();
        }
    }

    @Override
    public List<Article> findAll() {
        log.debug("Finding all articles");

        List<Article> articles = repository.findAll().stream()
                .map(mapper::toDomain)
                .toList();

        log.info("Found {} articles", articles.size());
        return articles;
    }

    @Override
    public boolean deleteById(String id) {
        log.debug("Attempting to delete article with ID: {}", id);

        if (id == null || id.isEmpty()) {
            log.error("Attempted to delete article with null or empty ID");
            return false;
        }

        try {
            ObjectId objectId = new ObjectId(id);
            if (repository.existsById(objectId)) {
                repository.deleteById(objectId);
                log.info("Article deleted successfully with ID: {}", id);
                return true;
            }
            log.debug("Article not found for deletion with ID: {}", id);
        } catch (IllegalArgumentException _) {
            log.error("Invalid ObjectId format for deletion: {}", id);
        }
        return false;
    }

    @Override
    public boolean existsById(String id) {
        log.debug("Checking if article exists with ID: {}", id);

        if (id == null || id.isEmpty()) {
            log.error("Attempted to check existence with null or empty ID");
            return false;
        }

        try {
            ObjectId objectId = new ObjectId(id);
            boolean exists = repository.existsById(objectId);
            log.debug("Article exists check for ID {}: {}", id, exists);
            return exists;
        } catch (IllegalArgumentException _) {
            log.error("Invalid ObjectId format for existence check: {}", id);
            return false;
        }
    }
}
