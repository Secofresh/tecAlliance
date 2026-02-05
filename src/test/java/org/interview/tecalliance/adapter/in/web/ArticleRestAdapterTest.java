package org.interview.tecalliance.adapter.in.web;

import org.interview.tecalliance.application.port.in.ArticleUseCase;
import org.interview.tecalliance.domain.model.article.Article;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ArticleRestAdapterTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @Autowired
    private ArticleUseCase articleUseCase;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + port + "/api/v1/articles";
        restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    @Test
    void testCreateArticle() {
        Article article = new Article(null, "Laptop", "Best laptop ever!",
                new BigDecimal("500.00"), new BigDecimal("800.00"), new BigDecimal("0.19"));

        Article response = restClient.post()
                .contentType(MediaType.APPLICATION_JSON)
                .body(article)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (_, resp) -> {
                    throw new RuntimeException("Failed with status: " + resp.getStatusCode());
                })
                .body(Article.class);

        assertNotNull(response);
        assertNotNull(response.getId());
        assertEquals("Laptop", response.getName());
    }

    @Test
    void testGetAllArticles() {
        Article[] response = restClient.get()
                .retrieve()
                .body(Article[].class);

        assertNotNull(response);
    }

    @Test
    void testGetArticleById() {
        Article article = new Article(null, "Mouse", "Wireless mouse",
                new BigDecimal("10.00"), new BigDecimal("25.00"), new BigDecimal("0.19"));
        Article created = articleUseCase.createArticle(article);

        Article response = restClient.get()
                .uri("/" + created.getId())
                .retrieve()
                .body(Article.class);

        assertNotNull(response);
        assertEquals("Mouse", response.getName());
    }

    @Test
    void testUpdateArticle() {
        Article article = new Article(null, "Keyboard", "Mechanical keyboard",
                new BigDecimal("30.00"), new BigDecimal("80.00"), new BigDecimal("0.19"));
        Article created = articleUseCase.createArticle(article);

        created.setName("Updated Keyboard");
        created.setSalesPrice(new BigDecimal("90.00"));

        Article response = restClient.put()
                .uri("/" + created.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .body(created)
                .retrieve()
                .body(Article.class);

        assertNotNull(response);
        assertEquals("Updated Keyboard", response.getName());
        assertEquals(new BigDecimal("90.00"), response.getSalesPrice());
    }

    @Test
    void testDeleteArticle() {
        Article article = new Article(null, "Monitor", "4K Monitor",
                new BigDecimal("200.00"), new BigDecimal("400.00"), new BigDecimal("0.19"));
        Article created = articleUseCase.createArticle(article);

        restClient.delete()
                .uri("/" + created.getId())
                .retrieve()
                .toBodilessEntity();

        assertFalse(articleUseCase.existsById(created.getId()));
    }

    @Test
    void testGetNonExistentArticle() {
        try {
            restClient.get()
                    .uri("/999999")
                    .retrieve()
                    .body(Article.class);
            fail("Expected 404 error");
        } catch (Exception e) {
            assertTrue(e.getMessage().contains("404"));
        }
    }
}
