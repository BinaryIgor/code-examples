package com.binaryigor.vembeddingswithpostgres.generators;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingInputData;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingModel;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OpenAIVectorEmbeddingsGenerator implements VectorEmbeddingsGenerator {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIVectorEmbeddingsGenerator.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String embeddingsUrl;
    private final String apiKey;

    public OpenAIVectorEmbeddingsGenerator(HttpClient httpClient, ObjectMapper objectMapper, String embeddingsUrl, String apiKey) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.embeddingsUrl = embeddingsUrl;
        this.apiKey = apiKey;
    }

    @Override
    public boolean supports(VectorEmbeddingModel model) {
        return model == VectorEmbeddingModel.OPEN_AI_TEXT_3_SMALL;
    }

    @Override
    public List<Float> generate(VectorEmbeddingModel model, String input) throws Exception {
        try {
            var response = executeEmbeddingsRequest(input, model.apiName);
            return objectMapper.readValue(response.body(), EmbeddingsResponse.class).data().getFirst().embedding();
        } catch (Throwable e) {
            logger.error("Problem while generating OpenAI embedding: ", e);
            throw new Exception("Problem while generating OpenAI embedding: " + e.getMessage());
        }
    }

    private HttpResponse<String> executeEmbeddingsRequest(Object embeddingsInput,
                                                          String embeddingsModel) throws java.lang.Exception {
        var body = objectMapper.writeValueAsString(Map.of("input", embeddingsInput, "model", embeddingsModel));
        var request = HttpRequest.newBuilder()
            .uri(new URI(embeddingsUrl))
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer " + apiKey)
            .build();

        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Non 200 embeddings response code: %d, body: %s".formatted(response.statusCode(), response.body()));
        }
        return response;
    }

    @Override
    public Map<VectorEmbeddingInputData, List<Float>> generateBatch(VectorEmbeddingModel model, List<VectorEmbeddingInputData> inputs) throws Exception {
        try {
            var input = inputs.stream().map(VectorEmbeddingInputData::data).toList();
            var response = executeEmbeddingsRequest(input, model.apiName);
            var embeddings = objectMapper.readValue(response.body(), EmbeddingsResponse.class).data();

            return embeddings.stream().map(e -> {
                    var eInput = inputs.get(e.index());
                    return Map.entry(eInput, e.embedding());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } catch (Throwable e) {
            logger.error("Problem while generating OpenAI embedding: ", e);
            throw new Exception("Problem while generating OpenAI embedding: " + e.getMessage());
        }
    }

    record EmbeddingsResponse(List<EmbeddingResponseData> data) {
    }

    record EmbeddingResponseData(int index, List<Float> embedding) {
    }
}
