package org.interview.tecalliance.adapter.out.persistence.mongodb;

import lombok.RequiredArgsConstructor;
import org.interview.tecalliance.adapter.out.persistence.mongodb.entity.ArticleEntity;
import org.interview.tecalliance.adapter.out.persistence.mongodb.repository.ArticleMongoRepository;
import org.interview.tecalliance.application.port.out.ArticlePersistencePort;
import org.interview.tecalliance.domain.model.article.Article;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MongoArticlePersistenceAdapter implements ArticlePersistencePort {

    private final ArticleMongoRepository repository;

    @Override
    public Article save(Article article) {
        if (article.getDiscounts() == null) {
            article.setDiscounts(new ArrayList<>());
        }
        ArticleEntity entity = ArticleEntity.fromDomain(article);
        ArticleEntity saved = repository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Optional<Article> findById(Long id) {
        return repository.findById(id.toString())
                .map(ArticleEntity::toDomain);
    }

    @Override
    public List<Article> findAll() {
        return repository.findAll().stream()
                .map(ArticleEntity::toDomain)
                .toList();
    }

    @Override
    public boolean deleteById(Long id) {
        if (repository.existsById(id.toString())) {
            repository.deleteById(id.toString());
            return true;
        }
        return false;
    }

    @Override
    public boolean existsById(Long id) {
        return repository.existsById(id.toString());
    }

}
