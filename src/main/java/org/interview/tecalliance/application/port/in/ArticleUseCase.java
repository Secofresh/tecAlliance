package org.interview.tecalliance.application.port.in;

import org.interview.tecalliance.domain.model.article.Article;
import org.interview.tecalliance.domain.model.article.ArticleWithPrice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface ArticleUseCase {

    Article createArticle(Article article);

    Optional<Article> getArticleById(Long id);

    List<Article> getAllArticles();

    List<ArticleWithPrice> getArticlesWithPrices(LocalDate date);

    List<Article> getArticlesWithDiscountOn(LocalDate date);

    Optional<Article> updateArticle(Long id, Article article);

    boolean deleteArticle(Long id);

    boolean existsById(Long id);
}
