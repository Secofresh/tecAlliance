package org.interview.tecalliance.adapter.config;

import org.interview.tecalliance.application.port.in.ArticleUseCase;
import org.interview.tecalliance.application.port.out.ArticlePersistencePort;
import org.interview.tecalliance.application.service.ArticleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HexagonalArchitectureConfig {

    @Bean
    public ArticleUseCase articleUseCase(ArticlePersistencePort persistencePort) {
        return new ArticleService(persistencePort);
    }
}
