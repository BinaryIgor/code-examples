package com.binaryigor.vembeddingswithpostgres.generators;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingInputData;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingModel;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PerformanceTestVectorEmbeddingsGenerator implements VectorEmbeddingsGenerator {

    @Override
    public boolean supports(VectorEmbeddingModel model) {
        return model == VectorEmbeddingModel.PERFORMANCE_TEST;
    }

    @Override
    public List<Float> generate(VectorEmbeddingModel model, String input) throws Exception {
        var embedding = new ArrayList<Float>(VectorEmbeddingModel.PERFORMANCE_TEST.dimensions);

        var expandedInput = expandedInput(input);

        for (int i = 0; i < VectorEmbeddingModel.PERFORMANCE_TEST.dimensions; i++) {
            var c = expandedInput.charAt(i);
            var dim = c / (Character.MAX_VALUE * 1.0f);
            embedding.add(dim);
        }

        return embedding;
    }

    private String expandedInput(String input) {
        if (input == null || input.isEmpty()) {
            throw new IllegalArgumentException("Non empty input is required");
        }
        var expandedInput = new StringBuilder(input);
        while (expandedInput.length() < VectorEmbeddingModel.PERFORMANCE_TEST.dimensions) {
            expandedInput.append(input);
        }
        return expandedInput.toString();
    }

    @Override
    public Map<VectorEmbeddingInputData, List<Float>> generateBatch(VectorEmbeddingModel model, List<VectorEmbeddingInputData> inputs) throws Exception {
        return inputs.stream()
            .map(iData -> Map.entry(iData, generate(model, iData.data())))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
