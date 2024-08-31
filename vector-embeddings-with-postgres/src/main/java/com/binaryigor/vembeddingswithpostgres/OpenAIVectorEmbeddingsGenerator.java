package com.binaryigor.vembeddingswithpostgres;

import java.util.List;

public class OpenAIVectorEmbeddingsGenerator implements VectorEmbeddingsGenerator{

    @Override
    public boolean supports(VectorEmbeddingModel model) {
        return false;
    }

    @Override
    public List<Float> generate(VectorEmbeddingModel model, String input) {
        return List.of();
    }
}
