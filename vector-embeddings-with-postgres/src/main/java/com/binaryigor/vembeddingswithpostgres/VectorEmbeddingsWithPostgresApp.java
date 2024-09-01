package com.binaryigor.vembeddingswithpostgres;

import com.binaryigor.vembeddingswithpostgres.data.AmazonReviewsVectorEmbeddingsDataSource;
import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingDataRepository;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingsRepository;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingsService;
import com.binaryigor.vembeddingswithpostgres.generator.OpenAIVectorEmbeddingsGenerator;
import com.binaryigor.vembeddingswithpostgres.generator.VectorEmbeddingsGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.net.http.HttpClient;
import java.util.List;

@SpringBootApplication
public class VectorEmbeddingsWithPostgresApp {

    public static void main(String[] args) {
        SpringApplication.run(VectorEmbeddingsWithPostgresApp.class, args);
    }

    @Bean
    VectorEmbeddingDataRepository vectorEmbeddingsDataRepository(JdbcClient jdbcClient,
                                                                 ObjectMapper objectMapper) {
        return new VectorEmbeddingDataRepository(jdbcClient, objectMapper);
    }

    @Bean
    AmazonReviewsVectorEmbeddingsDataSource amazonReviewsVectorEmbeddingsDataSource(VectorEmbeddingDataRepository dataRepository,
                                                                                    @Value("${data.amazon-reviews.batch-load-size}")
                                                                                    int batchLoadSize) {
        return new AmazonReviewsVectorEmbeddingsDataSource(dataRepository, batchLoadSize);
    }

    @Bean(initMethod = "initDb")
    VectorEmbeddingsRepository vectorEmbeddingsRepository(JdbcClient jdbcClient) {
        return new VectorEmbeddingsRepository(jdbcClient);
    }

    @Bean
    VectorEmbeddingsService vectorEmbeddingsService(VectorEmbeddingsRepository repository,
                                                    List<VectorEmbeddingsGenerator> generators) {
        return new VectorEmbeddingsService(repository, generators);
    }

    @ConditionalOnProperty(value = "generators.open-ai.enabled", havingValue = "true")
    @Bean
    VectorEmbeddingsGenerator openAIEmbeddingsGenerator(ObjectMapper objectMapper,
                                                        @Value("${generators.open-ai.embeddings-url}")
                                                        String openAIEmbeddingsUrl,
                                                        @Value("${generators.open-ai.api-key}")
                                                        String openAIApiKey) {
        return new OpenAIVectorEmbeddingsGenerator(HttpClient.newHttpClient(), objectMapper,
            openAIEmbeddingsUrl, openAIApiKey);
    }
}
