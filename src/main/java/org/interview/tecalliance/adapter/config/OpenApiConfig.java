package org.interview.tecalliance.adapter.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI articleManagementOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Article Management API")
                        .description("REST API for managing articles with pricing and discount functionality. " +
                                "This API follows hexagonal/ports and adapters architecture.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TecAlliance Team")
                                .email("support@tecalliance.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local development server")
                ));
    }
}
