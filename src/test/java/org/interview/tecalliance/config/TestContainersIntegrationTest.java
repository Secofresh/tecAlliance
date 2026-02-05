package org.interview.tecalliance.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.testcontainers.containers.MongoDBContainer;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(TestContainersConfiguration.class)
class TestContainersIntegrationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoDBContainer mongoDBContainer;

    @Test
    void testMongoDBContainerIsRunning() {
        assertNotNull(mongoDBContainer, "MongoDB container should be injected");
        assertTrue(mongoDBContainer.isRunning(), "MongoDB container should be running");

        String containerImage = mongoDBContainer.getDockerImageName();
        assertTrue(containerImage.contains("mongo"), "Container should use MongoDB image");

        System.out.println("✓ MongoDB Testcontainer is running: " + containerImage);
        System.out.println("✓ Container ID: " + mongoDBContainer.getContainerId());
        System.out.println("✓ Mapped Port: " + mongoDBContainer.getFirstMappedPort());
    }

    @Test
    void testMongoTemplateCanConnectToContainer() {
        assertNotNull(mongoTemplate, "MongoTemplate should be injected");

        assertDoesNotThrow(() -> {
            var collections = mongoTemplate.getCollectionNames();
            assertNotNull(collections, "Should be able to get collection names");
            System.out.println("✓ Successfully connected to MongoDB via Testcontainers");
            System.out.println("✓ Available collections: " + collections);
        });
    }

    @Test
    void testCanWriteAndReadFromContainer() {
        String testCollection = "testCollection";

        if (mongoTemplate.collectionExists(testCollection)) {
            mongoTemplate.dropCollection(testCollection);
        }

        mongoTemplate.createCollection(testCollection);
        assertTrue(mongoTemplate.collectionExists(testCollection),
                   "Should be able to create collection in Testcontainer");

        TestDocument doc = new TestDocument("test-id", "Hello Testcontainers!");
        mongoTemplate.insert(doc, testCollection);

        TestDocument retrieved = mongoTemplate.findById("test-id", TestDocument.class, testCollection);
        assertNotNull(retrieved, "Should be able to retrieve inserted document");
        assertEquals("Hello Testcontainers!", retrieved.message());

        System.out.println("✓ Successfully wrote and read data from MongoDB Testcontainer");

        mongoTemplate.dropCollection(testCollection);
    }

    private record TestDocument(String id, String message) {}
}
