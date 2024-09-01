package com.binaryigor.vembeddingswithpostgres.generator;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingInputData;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingModel;

import java.util.List;
import java.util.Map;

public interface VectorEmbeddingsGenerator {

    boolean supports(VectorEmbeddingModel model);

    List<Float> generate(VectorEmbeddingModel model, String input) throws Exception;

    Map<VectorEmbeddingInputData, List<Float>> generateBatch(VectorEmbeddingModel model, List<VectorEmbeddingInputData> inputs) throws Exception;

    class Exception extends RuntimeException {
        public Exception(String message) {
            super(message);
        }
    }
}
