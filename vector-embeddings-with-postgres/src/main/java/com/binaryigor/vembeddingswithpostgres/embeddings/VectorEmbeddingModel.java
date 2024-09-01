package com.binaryigor.vembeddingswithpostgres.embeddings;

public enum VectorEmbeddingModel {
    OPEN_AI_TEXT_3_SMALL(1536, "text-embedding-3-small");

    public final int dimensions;
    public final String apiName;

    VectorEmbeddingModel(int dimensions, String apiName) {
        this.dimensions = dimensions;
        this.apiName = apiName;
    }
}
