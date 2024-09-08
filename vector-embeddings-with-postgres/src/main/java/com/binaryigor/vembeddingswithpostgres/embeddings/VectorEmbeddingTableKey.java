package com.binaryigor.vembeddingswithpostgres.embeddings;

public record VectorEmbeddingTableKey(VectorEmbeddingModel model, String dataSource) {
}
