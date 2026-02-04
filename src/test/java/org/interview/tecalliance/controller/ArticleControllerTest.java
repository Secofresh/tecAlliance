package org.interview.tecalliance.controller;

import org.interview.tecalliance.model.Article;
import org.interview.tecalliance.service.ArticleService;
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
class ArticleControllerTest {

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @Autowired
    private ArticleService articleService;

    @BeforeEach
    void setUp() {
        String baseUrl = "http://localhost:" + port + "/api/articles";
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
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
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
        // Create an article first
        Article article = new Article(null, "Mouse", "Wireless mouse",
                new BigDecimal("10.00"), new BigDecimal("25.00"), new BigDecimal("0.19"));
        Article created = articleService.createArticle(article);

        // Get the article
        Article response = restClient.get()
                .uri("/" + created.getId())
                .retrieve()
                .body(Article.class);

        assertNotNull(response);
        assertEquals("Mouse", response.getName());
    }

    @Test
    void testUpdateArticle() {
        // Create an article first
        Article article = new Article(null, "Keyboard", "Mechanical keyboard",
                new BigDecimal("30.00"), new BigDecimal("80.00"), new BigDecimal("0.19"));
        Article created = articleService.createArticle(article);

        // Update the article
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
        // Create an article first
        Article article = new Article(null, "Monitor", "4K Monitor",
                new BigDecimal("200.00"), new BigDecimal("450.00"), new BigDecimal("0.19"));
        Article created = articleService.createArticle(article);

        // Delete the article
        restClient.delete()
                .uri("/" + created.getId())
                .retrieve()
                .toBodilessEntity();

        // Verify it's deleted - should throw an exception for 404
        try {
            restClient.get()
                    .uri("/" + created.getId())
                    .retrieve()
                    .body(Article.class);
            fail("Expected exception for deleted article");
        } catch (Exception _) {
            // Expected - article not found
        }
    }

    @Test
    void testGetNonExistentArticle() {
        try {
            restClient.get()
                    .uri("/999999")
                    .retrieve()
                    .body(Article.class);
            fail("Expected exception for non-existent article");
        } catch (Exception _) {
            // Expected - article not found
        }
    }
}
