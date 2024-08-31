package com.binaryigor.vembeddingswithpostgres;

import java.time.Duration;
import java.util.List;

public record VectorEmbeddingsSearchResult(Duration searchTime,
                                           List<VectorEmbeddingSearchResult> results) {
}
