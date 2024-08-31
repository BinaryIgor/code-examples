package com.binaryigor.vembeddingswithpostgres;

import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class RandomVectorEmbeddingsGenerator implements VectorEmbeddingsGenerator {

    private static final Random RANDOM = new Random();

    @Override
    public boolean supports(VectorEmbeddingModel model) {
        return true;
    }

    @Override
    public List<Float> generate(VectorEmbeddingModel model, String input) {
        return Stream.generate(RANDOM::nextFloat)
            .limit(model.dimensions)
            .toList();
    }
}
