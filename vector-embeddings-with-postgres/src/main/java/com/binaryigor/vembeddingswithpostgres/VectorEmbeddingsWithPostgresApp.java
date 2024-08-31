package com.binaryigor.vembeddingswithpostgres;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;

@SpringBootApplication
public class VectorEmbeddingsWithPostgresApp {

    public static void main(String[] args) {
        SpringApplication.run(VectorEmbeddingsWithPostgresApp.class, args);
    }

    @Bean
    VectorEmbeddingsGenerator vectorEmbeddingsGenerator() {
        return new RandomVectorEmbeddingsGenerator();
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
}
