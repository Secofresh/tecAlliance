package org.interview.tecalliance.config;

import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TestContainersConfiguration.class);
    private MongoDBContainer container;

    @Bean
    @ServiceConnection
    public MongoDBContainer mongoDBContainer() {
        try {
            log.info("Creating MongoDB Test container...");
            container = new MongoDBContainer(DockerImageName.parse("mongo:8.0"))
                    .withExposedPorts(27017)
                    .withReuse(false);

            log.info("MongoDB Test container created successfully");
            return container;
        } catch (Exception e) {
            String errorMessage = "Failed to create MongoDB Test container. " +
                    "Ensure Docker is running and accessible. Error: " + e.getMessage();
            log.error(errorMessage, e);
            throw new RuntimeException(errorMessage, e);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (container != null && container.isRunning()) {
            try {
                log.info("Stopping MongoDB Testcontainer...");
                container.stop();
                log.info("MongoDB Testcontainer stopped successfully");
            } catch (Exception e) {
                log.warn("Error while stopping MongoDB Testcontainer: {}", e.getMessage(), e);
            }
        }
    }
}
