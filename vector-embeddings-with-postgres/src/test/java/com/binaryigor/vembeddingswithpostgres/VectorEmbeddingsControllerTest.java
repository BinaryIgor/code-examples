package com.binaryigor.vembeddingswithpostgres;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class VectorEmbeddingsControllerTest extends IntegrationTest {

    private static final VectorEmbeddingModel TESTED_MODEL = VectorEmbeddingModel.OPEN_AI_TEXT_3_SMALL;
    @Autowired
    private VectorEmbeddingsRepository vectorEmbeddingsRepository;

    @Test
    void generatesVectorEmbeddings() {
        generateRandomVectorEmbeddings(100);

        var savedEmbedding = randomVectorEmbeddingFromDb();

        var searchResults = rawSearchVectorEmbeddings(savedEmbedding.embedding());

        Assertions.assertThat(searchResults)
            .hasSizeGreaterThan(1);
    }

    private void generateRandomVectorEmbeddings(int size) {
        var response = restTemplate.postForEntity("/vector-embeddings/generate-random?size=%s&model=%s".formatted(size, TESTED_MODEL),
            null, Void.class);
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        delay(250);
    }

    private VectorEmbedding randomVectorEmbeddingFromDb() {
        return vectorEmbeddingsRepository.allOfModel(TESTED_MODEL).getFirst();
    }

    private List<VectorEmbeddingSearchResult> rawSearchVectorEmbeddings(List<Float> input) {
        var response = restTemplate.postForEntity("/vector-embeddings/raw-search",
            new VectorEmbeddingsController.RawSearchRequest(input, TESTED_MODEL), VectorEmbeddingsSearchResult.class);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        return response.getBody().results();
    }
}
