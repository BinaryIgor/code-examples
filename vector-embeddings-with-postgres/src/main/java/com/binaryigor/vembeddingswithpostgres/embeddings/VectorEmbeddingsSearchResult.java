package com.binaryigor.vembeddingswithpostgres.embeddings;

import java.time.Duration;
import java.util.List;

public record VectorEmbeddingsSearchResult(Duration searchTime,
                                           List<VectorEmbeddingSearchResult> results) {
}
