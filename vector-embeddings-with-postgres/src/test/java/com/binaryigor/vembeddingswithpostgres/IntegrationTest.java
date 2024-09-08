package com.binaryigor.vembeddingswithpostgres;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingDataRepository;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingRepository;
import com.binaryigor.vembeddingswithpostgres.generator.RandomVectorEmbeddingsGenerator;
import com.binaryigor.vembeddingswithpostgres.generator.VectorEmbeddingsGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@ActiveProfiles(value = {"integration"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = IntegrationTest.TestConfig.class)
@AutoConfigureWireMock(port = 0)
public abstract class IntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>(
        DockerImageName.parse("pgvector/pgvector:pg16")
            .asCompatibleSubstituteFor("postgres"));

    static {
        POSTGRESQL_CONTAINER.start();
        System.setProperty("DB_URL", POSTGRESQL_CONTAINER.getJdbcUrl());
        System.setProperty("DB_USERNAME", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("DB_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
    }

    @Autowired
    protected TestVectorEmbeddingsDataSource testVectorEmbeddingsDataSource;
    @Autowired
    protected TestRestTemplate restTemplate;
    @Autowired
    protected JdbcClient jdbcClient;
    @Autowired
    protected ObjectMapper objectMapper;
    @Autowired
    private VectorEmbeddingRepository vectorEmbeddingRepository;

    @BeforeEach
    void setup() {
        jdbcClient.sql("TRUNCATE vembedding_data").update();
        vectorEmbeddingRepository.tables()
            .forEach(t -> jdbcClient.sql("TRUNCATE %s".formatted(t)));
    }

    public void asyncEndpointDelay() {
        delay(100);
    }

    public void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        TestVectorEmbeddingsDataSource testVectorEmbeddingsDataSource(VectorEmbeddingDataRepository dataRepository) {
            return new TestVectorEmbeddingsDataSource(dataRepository);
        }

        @Bean
        VectorEmbeddingsGenerator vectorEmbeddingsGenerator() {
            return new RandomVectorEmbeddingsGenerator();
        }
    }
}
