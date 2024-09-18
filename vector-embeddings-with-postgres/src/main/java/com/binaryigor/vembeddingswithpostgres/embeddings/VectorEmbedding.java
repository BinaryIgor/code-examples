package com.binaryigor.vembeddingswithpostgres.embeddings;

import java.util.List;

public record VectorEmbedding(String id, List<Float> embedding, String embeddingInput) {
}
