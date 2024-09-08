package com.binaryigor.vembeddingswithpostgres.generators;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingInputData;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingModel;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsGenerator;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
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

    @Override
    public Map<VectorEmbeddingInputData, List<Float>> generateBatch(VectorEmbeddingModel model, List<VectorEmbeddingInputData> inputs) throws Exception {
        return inputs.stream()
            .collect(Collectors.toMap(e -> e, e -> generate(model, e.data())));
    }
}
