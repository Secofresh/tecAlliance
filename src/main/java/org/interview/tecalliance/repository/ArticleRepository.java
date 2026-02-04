package org.interview.tecalliance.repository;

import org.interview.tecalliance.model.Article;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Repository
public class ArticleRepository {

    private final Map<Long, Article> articles = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public Article save(Article article) {
        if (article.getId() == null) {
            article.setId(idGenerator.getAndIncrement());
        }
        articles.put(article.getId(), article);
        return article;
    }

    public Optional<Article> findById(Long id) {
        return Optional.ofNullable(articles.get(id));
    }

    public List<Article> findAll() {
        return new ArrayList<>(articles.values());
    }

    public boolean deleteById(Long id) {
        return articles.remove(id) != null;
    }

    public boolean existsById(Long id) {
        return articles.containsKey(id);
    }
}
