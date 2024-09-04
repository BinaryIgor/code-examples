package com.binaryigor.vembeddingswithpostgres.generator;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingInputData;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.http.HttpClient;
import java.util.List;
import java.util.Map;

public class GeminiVectorEmbeddingsGenerator implements VectorEmbeddingsGenerator {

    private static final Logger logger = LoggerFactory.getLogger(GeminiVectorEmbeddingsGenerator.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String embeddingsUrl;
    private final String apiKey;

    public GeminiVectorEmbeddingsGenerator(HttpClient httpClient, ObjectMapper objectMapper, String embeddingsUrl, String apiKey) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.embeddingsUrl = embeddingsUrl;
        this.apiKey = apiKey;
    }

    @Override
    public boolean supports(VectorEmbeddingModel model) {
        return model == VectorEmbeddingModel.GEMINI_TEXT_004;
    }

    @Override
    public List<Float> generate(VectorEmbeddingModel model, String input) throws Exception {
        return List.of();
    }

    @Override
    public Map<VectorEmbeddingInputData, List<Float>> generateBatch(VectorEmbeddingModel model, List<VectorEmbeddingInputData> inputs) throws Exception {
        return Map.of();
    }
}
