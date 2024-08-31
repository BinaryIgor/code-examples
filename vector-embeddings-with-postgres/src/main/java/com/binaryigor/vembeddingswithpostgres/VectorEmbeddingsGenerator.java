package com.binaryigor.vembeddingswithpostgres;

import java.util.List;

public interface VectorEmbeddingsGenerator {

    boolean supports(VectorEmbeddingModel model);

    List<Float> generate(VectorEmbeddingModel model, String input);
}
