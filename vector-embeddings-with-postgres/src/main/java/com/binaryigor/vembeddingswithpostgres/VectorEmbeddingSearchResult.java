package com.binaryigor.vembeddingswithpostgres;

import java.util.UUID;

public record VectorEmbeddingSearchResult(UUID id, String embeddingInput, float distance) {
}
