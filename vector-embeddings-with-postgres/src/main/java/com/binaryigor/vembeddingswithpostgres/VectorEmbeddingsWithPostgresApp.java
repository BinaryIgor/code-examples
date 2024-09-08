package com.binaryigor.vembeddingswithpostgres;

import com.binaryigor.vembeddingswithpostgres.data.BooksVectorEmbeddingsDataSource;
import com.binaryigor.vembeddingswithpostgres.data.PerformanceTestVectorEmbeddingsDataSource;
import com.binaryigor.vembeddingswithpostgres.data.TheVectorEmbeddingsSupportedDataSources;
import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingDataRepository;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingRepository;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingService;
import com.binaryigor.vembeddingswithpostgres.generators.GoogleVectorEmbeddingsGenerator;
import com.binaryigor.vembeddingswithpostgres.generators.OpenAIVectorEmbeddingsGenerator;
import com.binaryigor.vembeddingswithpostgres.generators.PerformanceTestVectorEmbeddingsGenerator;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsDataSource;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsGenerator;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsSupportedDataSources;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;

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
    BooksVectorEmbeddingsDataSource booksVectorEmbeddingsDataSource(VectorEmbeddingDataRepository dataRepository,
                                                                    @Value("${data.books.batch-load-size}")
                                                                    int batchLoadSize) {
        return new BooksVectorEmbeddingsDataSource(dataRepository, batchLoadSize);
    }

    @Bean
    PerformanceTestVectorEmbeddingsDataSource performanceTestVectorEmbeddingsDataSource(@Value("${data.performance-test.data-size}")
                                                                                        int dataSize) {
        return new PerformanceTestVectorEmbeddingsDataSource(dataSize);
    }

    @Bean
    VectorEmbeddingsSupportedDataSources vectorEmbeddingsSupportedDataSources(List<VectorEmbeddingsDataSource> vectorEmbeddingsDataSources) {
        return new TheVectorEmbeddingsSupportedDataSources(vectorEmbeddingsDataSources);
    }

    @Bean(initMethod = "initDb")
    VectorEmbeddingRepository vectorEmbeddingsRepository(JdbcClient jdbcClient,
                                                         PlatformTransactionManager transactionManager,
                                                         VectorEmbeddingsSupportedDataSources vectorEmbeddingsSupportedDataSources) {
        return new VectorEmbeddingRepository(jdbcClient, transactionManager, vectorEmbeddingsSupportedDataSources);
    }

    @Bean
    VectorEmbeddingService vectorEmbeddingsService(VectorEmbeddingRepository repository,
                                                   List<VectorEmbeddingsGenerator> generators) {
        return new VectorEmbeddingService(repository, generators);
    }

    @ConditionalOnProperty(value = "generators.open-ai.enabled", havingValue = "true")
    @Bean
    VectorEmbeddingsGenerator openAIEmbeddingsGenerator(ObjectMapper objectMapper,
                                                        @Value("${generators.open-ai.embeddings-url}")
                                                        String embeddingsUrl,
                                                        @Value("${generators.open-ai.api-key}")
                                                        String apiKey) {
        return new OpenAIVectorEmbeddingsGenerator(HttpClient.newHttpClient(), objectMapper, embeddingsUrl, apiKey);
    }

    @ConditionalOnProperty(value = "generators.google.enabled", havingValue = "true")
    @Bean
    VectorEmbeddingsGenerator googleEmbeddingsGenerator(ObjectMapper objectMapper,
                                                        @Value("${generators.google.base-url}")
                                                        String baseUrl,
                                                        @Value("${generators.google.api-key}")
                                                        String apiKey) {
        return new GoogleVectorEmbeddingsGenerator(HttpClient.newHttpClient(), objectMapper, baseUrl, apiKey);
    }

    @Bean
    PerformanceTestVectorEmbeddingsGenerator performanceTestVectorEmbeddingsGenerator() {
        return new PerformanceTestVectorEmbeddingsGenerator();
    }
}
