package org.interview.tecalliance.application.port.out;

import org.interview.tecalliance.domain.model.article.Article;

import java.util.List;
import java.util.Optional;

public interface ArticlePersistencePort {

    Article save(Article article);

    Optional<Article> findById(Long id);

    List<Article> findAll();

    boolean deleteById(Long id);

    boolean existsById(Long id);
}
