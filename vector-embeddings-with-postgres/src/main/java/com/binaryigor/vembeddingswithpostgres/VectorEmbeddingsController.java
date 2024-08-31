package com.binaryigor.vembeddingswithpostgres;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vector-embeddings")
public class VectorEmbeddingsController {

    private final VectorEmbeddingsService service;

    public VectorEmbeddingsController(VectorEmbeddingsService service) {
        this.service = service;
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/generate-random")
    void generateRandomEmbeddings(@RequestParam("model") VectorEmbeddingModel model,
                                  @RequestParam("size") int size) {
        Thread.startVirtualThread(() -> service.generateAndSaveRandomEmbeddings(model, size, 1000));
    }

    @PostMapping("/raw-search")
    VectorEmbeddingsSearchResult rawSearch(@RequestBody RawSearchRequest request) {
        var result = Extensions.timed(() -> service.rawSearch(request.model, request.input, 5));
        return new VectorEmbeddingsSearchResult(result.time(), result.result());
    }

    @PostMapping("/similar-to-embedding-search")
    VectorEmbeddingsSearchResult similarToEmbeddingSearch(@RequestBody SimilarToVectorSearchRequest request) {
        var result = Extensions.timed(() -> service.similarToEmbeddingSearch(request.model, request.embeddingId, 5));
        return new VectorEmbeddingsSearchResult(result.time(), result.result());
    }

    public record RawSearchRequest(List<Float> input, VectorEmbeddingModel model) {
    }

    public record SimilarToVectorSearchRequest(UUID embeddingId, VectorEmbeddingModel model) {
    }
}
