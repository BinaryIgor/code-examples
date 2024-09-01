package com.binaryigor.vembeddingswithpostgres;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingsDataSource;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingModel;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingsSearchResult;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingsService;
import com.binaryigor.vembeddingswithpostgres.shared.Extensions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vector-embeddings")
public class VectorEmbeddingsController {

    private static final Logger logger = LoggerFactory.getLogger(VectorEmbeddingsController.class);
    private final List<VectorEmbeddingsDataSource> dataSources;
    private final VectorEmbeddingsService service;

    public VectorEmbeddingsController(List<VectorEmbeddingsDataSource> dataSources,
                                      VectorEmbeddingsService service) {
        this.dataSources = dataSources;
        this.service = service;
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/load-data")
    void loadForEmbeddingsData(@RequestBody LoadDataRequest request) {
        var dataSource = embeddingsDataSource(request.type());
        Thread.startVirtualThread(() -> {
            try {
                dataSource.load(request.path());
            } catch (Exception e) {
                logger.error("Fail to load %s data for vector embeddings:".formatted(request.type()), e);
            }
        });
    }

    private VectorEmbeddingsDataSource embeddingsDataSource(String type) {
        return dataSources.stream()
            .filter(s -> s.dataType().equals(type))
            .findFirst()
            .orElseThrow(() -> new DataTypeNotSupportedException(type));
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @PostMapping("/generate")
    void generateEmbeddings(@RequestParam("model") VectorEmbeddingModel model,
                            @RequestParam("dataType") String dataType,
                            @RequestParam(name = "batchSize", required = false, defaultValue = "500") int batchSize,
                            @RequestParam(name = "skip", required = false, defaultValue = "0") int skip) {
        var dataSource = embeddingsDataSource(dataType);
        Thread.startVirtualThread(() -> {
            try {
                service.generateAndSaveEmbeddings(model, dataSource.get(), batchSize, skip);
            } catch (Exception e) {
                logger.error("Problem while generating vector embeddings for {} data type, using {} model:",
                    dataType, model, e);
            }
        });
    }

    @PostMapping("/search")
    VectorEmbeddingsSearchResult search(@RequestBody SearchRequest request) {
        var result = Extensions.timed(() -> service.search(request.model, request.input, 5));
        return new VectorEmbeddingsSearchResult(result.time(), result.result());
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

    public record LoadDataRequest(String type, String path) {
    }

    public record SearchRequest(String input, VectorEmbeddingModel model) {
    }

    public record RawSearchRequest(List<Float> input, VectorEmbeddingModel model) {
    }

    public record SimilarToVectorSearchRequest(UUID embeddingId, VectorEmbeddingModel model) {
    }

    public static class DataTypeNotSupportedException extends RuntimeException {
        public DataTypeNotSupportedException(String type) {
            super("%s type is not supported".formatted(type));
        }
    }
}
