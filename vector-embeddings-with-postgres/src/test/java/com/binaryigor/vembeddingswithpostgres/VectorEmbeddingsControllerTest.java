package com.binaryigor.vembeddingswithpostgres;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingInputData;
import com.binaryigor.vembeddingswithpostgres.embeddings.*;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import java.util.List;

public class VectorEmbeddingsControllerTest extends IntegrationTest {

    private static final VectorEmbeddingModel TESTED_MODEL = VectorEmbeddingModel.OPEN_AI_TEXT_3_SMALL;
    private static final String TESTED_DATA_SOURCE = TestVectorEmbeddingsDataSource.DATA_TYPE;
    @Autowired
    private VectorEmbeddingRepository vectorEmbeddingsRepository;

    @Test
    void generatesVectorEmbeddings() {
        testVectorEmbeddingsDataSource.dataSource(List.of(
            new VectorEmbeddingInputData("1", "some-data-1"),
            new VectorEmbeddingInputData("2", "some-data-2")));

        loadVectorEmbeddings();
        generateVectorEmbeddings();

        var savedEmbedding = randomVectorEmbeddingFromDb();

        var searchResults = rawSearchVectorEmbeddings(savedEmbedding.embedding());

        Assertions.assertThat(searchResults)
            .hasSizeGreaterThan(1);
    }

    private void loadVectorEmbeddings() {
        var response = restTemplate.postForEntity("/vector-embeddings/load-data",
            new VectorEmbeddingsController.LoadDataRequest(testVectorEmbeddingsDataSource.dataType(), "dummy-path"), Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        asyncEndpointDelay();
    }

    private void generateVectorEmbeddings() {
        var response = restTemplate.postForEntity("/vector-embeddings/generate?size=%s&model=%s&dataType=%s"
                .formatted(1000, TESTED_MODEL, testVectorEmbeddingsDataSource.dataType()),
            null, Void.class);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

        asyncEndpointDelay();
    }

    private VectorEmbedding randomVectorEmbeddingFromDb() {
        return vectorEmbeddingsRepository.allOf(new VectorEmbeddingTableKey(TESTED_MODEL, TESTED_DATA_SOURCE)).getFirst();
    }

    private List<VectorEmbeddingSearchResult> rawSearchVectorEmbeddings(List<Float> input) {
        var response = restTemplate.postForEntity("/vector-embeddings/raw-search",
            new VectorEmbeddingsController.RawSearchRequest(input, TESTED_MODEL, TESTED_DATA_SOURCE, null), VectorEmbeddingsSearchResult.class);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        return response.getBody().results();
    }
}
