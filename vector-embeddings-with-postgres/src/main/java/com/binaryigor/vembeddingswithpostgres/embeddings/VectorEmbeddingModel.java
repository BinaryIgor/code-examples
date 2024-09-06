package com.binaryigor.vembeddingswithpostgres.embeddings;

public enum VectorEmbeddingModel {
    OPEN_AI_TEXT_3_SMALL(1536, "text-embedding-3-small"),
    GEMINI_TEXT_004(768, "models/text-embedding-004"),
    MISTRAL_EMBED(1024, "mistral-embed");

    public final int dimensions;
    public final String apiName;

    VectorEmbeddingModel(int dimensions, String apiName) {
        this.dimensions = dimensions;
        this.apiName = apiName;
    }
}
