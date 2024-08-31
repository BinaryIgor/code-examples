package com.binaryigor.vembeddingswithpostgres;

public enum VectorEmbeddingModel {
    OPEN_AI_TEXT_3_SMALL(1535, "text-embedding-3-small");

    final int dimensions;
    final String apiName;

    VectorEmbeddingModel(int dimensions, String apiName) {
        this.dimensions = dimensions;
        this.apiName = apiName;
    }
}
