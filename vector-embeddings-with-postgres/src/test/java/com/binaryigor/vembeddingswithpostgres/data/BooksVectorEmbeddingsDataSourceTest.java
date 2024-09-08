package com.binaryigor.vembeddingswithpostgres.data;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class BooksVectorEmbeddingsDataSourceTest {

    @Test
    void producesDesiredEmbeddingFromBookRecord() {
        var record = new BooksVectorEmbeddingsDataSource.BookRecord("Some Title",
            "Many authors, such as a nad b",
            "Rather long and elaborated description",
            "Random",
            "",
            Instant.now().toString(),
            "$4.99");

        var expectedEmbeddingData = """
            Some Title
            
            Rather long and elaborated description
            """.strip();

        Assertions.assertThat(record.embeddingData()).isEqualTo(expectedEmbeddingData);
    }
}
