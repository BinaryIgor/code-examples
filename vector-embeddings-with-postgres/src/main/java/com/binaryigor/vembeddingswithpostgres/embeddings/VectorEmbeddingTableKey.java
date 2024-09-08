package com.binaryigor.vembeddingswithpostgres.embeddings;

import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingModel;

public record VectorEmbeddingTableKey(VectorEmbeddingModel model, String dataSource) {
}
