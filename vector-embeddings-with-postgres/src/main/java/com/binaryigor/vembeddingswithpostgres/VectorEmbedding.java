package com.binaryigor.vembeddingswithpostgres;

import java.util.List;
import java.util.UUID;

public record VectorEmbedding(UUID id, List<Float> embedding, String embeddingInput) {
}
