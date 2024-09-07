package com.binaryigor.vembeddingswithpostgres.generator;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingInputData;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingModel;
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
import java.util.stream.IntStream;

public class GoogleVectorEmbeddingsGenerator implements VectorEmbeddingsGenerator {

    private static final Logger logger = LoggerFactory.getLogger(GoogleVectorEmbeddingsGenerator.class);
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final String apiKey;

    public GoogleVectorEmbeddingsGenerator(HttpClient httpClient, ObjectMapper objectMapper, String baseUrl, String apiKey) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.apiKey = apiKey;
    }

    @Override
    public boolean supports(VectorEmbeddingModel model) {
        return model == VectorEmbeddingModel.GOOGLE_TEXT_004;
    }

    @Override
    public List<Float> generate(VectorEmbeddingModel model, String input) throws Exception {
        try {
            var requestBody = objectMapper.writeValueAsString(new EmbedContentRequest(model.apiName,
                ContentRequest.of(input)));

            var request = HttpRequest.newBuilder()
                .uri(new URI("%s/v1beta/models/%s:embedContent?key=%s"
                    .formatted(baseUrl, model.apiName, apiKey)))
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            var response = executeRequest(request, WrappedContentEmbedding.class);

            return response.embedding().values();
        } catch (Throwable e) {
            logger.error("Problem while generating Google embedding: ", e);
            throw new Exception("Problem while generating Google embedding: " + e.getMessage());
        }
    }

    @Override
    public Map<VectorEmbeddingInputData, List<Float>> generateBatch(VectorEmbeddingModel model, List<VectorEmbeddingInputData> inputs) throws Exception {
        try {
            var requestBody = objectMapper.writeValueAsString(embedContentRequests(model, inputs));

            var request = HttpRequest.newBuilder()
                .uri(new URI("%s/v1beta/models/%s:batchEmbedContents?key=%s"
                    .formatted(baseUrl, model.apiName, apiKey)))
                .header("content-type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

            var response = executeRequest(request, ContentEmbeddings.class);

            var embeddings = response.embeddings().stream()
                .map(ContentEmbedding::values)
                .toList();

            return embeddingsAssociatedWithInputData(inputs, embeddings);
        } catch (Throwable e) {
            logger.error("Problem while generating Google embeddings: ", e);
            throw new Exception("Problem while generating Google embeddings: " + e.getMessage());
        }
    }

    private EmbedContentRequests embedContentRequests(VectorEmbeddingModel model,
                                                      List<VectorEmbeddingInputData> inputs) {
        return new EmbedContentRequests(inputs.stream()
            .map(e -> new EmbedContentRequest("models/" + model.apiName, ContentRequest.of(e.data())))
            .toList());
    }

    private <T> T executeRequest(HttpRequest request, Class<T> type) throws java.lang.Exception {
        var response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Non 200 embeddings response code: %d, body: %s".formatted(response.statusCode(), response.body()));
        }

        return objectMapper.readValue(response.body(), type);
    }

    private Map<VectorEmbeddingInputData, List<Float>> embeddingsAssociatedWithInputData(List<VectorEmbeddingInputData> inputData,
                                                                                         List<List<Float>> embeddings) {
        return IntStream.range(0, inputData.size())
            .mapToObj(i -> Map.entry(inputData.get(i), embeddings.get(i)))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    record ContentPart(String text) {
    }

    record ContentRequest(List<ContentPart> parts) {

        public static ContentRequest of(List<String> parts) {
            return new ContentRequest(parts.stream().map(ContentPart::new).toList());
        }

        public static ContentRequest of(String part) {
            return of(List.of(part));
        }
    }

    record EmbedContentRequest(String model, ContentRequest content) {
    }

    record EmbedContentRequests(List<EmbedContentRequest> requests) {
    }

    record ContentEmbeddings(List<ContentEmbedding> embeddings) {
    }

    record ContentEmbedding(List<Float> values) {
    }

    record WrappedContentEmbedding(ContentEmbedding embedding) {
    }
}
