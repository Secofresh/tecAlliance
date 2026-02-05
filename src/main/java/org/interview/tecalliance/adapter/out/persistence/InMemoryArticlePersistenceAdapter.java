package org.interview.tecalliance.adapter.out.persistence;

import org.interview.tecalliance.application.port.out.ArticlePersistencePort;
import org.interview.tecalliance.domain.model.article.Article;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class InMemoryArticlePersistenceAdapter implements ArticlePersistencePort {

    private final Map<Long, Article> articles = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    @Override
    public Article save(Article article) {
        if (article.getId() == null) {
            article.setId(idGenerator.getAndIncrement());
        }
        articles.put(article.getId(), article);
        return article;
    }

    @Override
    public Optional<Article> findById(Long id) {
        return Optional.ofNullable(articles.get(id));
    }

    @Override
    public List<Article> findAll() {
        return new ArrayList<>(articles.values());
    }

    @Override
    public boolean deleteById(Long id) {
        return articles.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        return articles.containsKey(id);
    }
}
