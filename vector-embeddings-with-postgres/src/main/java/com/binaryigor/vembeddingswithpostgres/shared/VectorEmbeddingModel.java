package com.binaryigor.vembeddingswithpostgres.shared;

public enum VectorEmbeddingModel {
    OPEN_AI_TEXT_3_SMALL(1536, "text-embedding-3-small"),
    GOOGLE_TEXT_004(768, "text-embedding-004"),
    PERFORMANCE_TEST(1536, "performance-test");

    public final int dimensions;
    public final String apiName;

    VectorEmbeddingModel(int dimensions, String apiName) {
        this.dimensions = dimensions;
        this.apiName = apiName;
    }
}
