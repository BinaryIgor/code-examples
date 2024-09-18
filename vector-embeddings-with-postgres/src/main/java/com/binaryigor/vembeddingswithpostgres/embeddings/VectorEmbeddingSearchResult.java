package com.binaryigor.vembeddingswithpostgres.embeddings;

public record VectorEmbeddingSearchResult(String id, String embeddingInput, float distance) {
}
