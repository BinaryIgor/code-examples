package com.binaryigor.vembeddingswithpostgres;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingData;
import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingDataRepository;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingModel;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingService;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingTableKey;
import com.binaryigor.vembeddingswithpostgres.embeddings.VectorEmbeddingsSearchResult;
import com.binaryigor.vembeddingswithpostgres.shared.Extensions;
import com.binaryigor.vembeddingswithpostgres.shared.ResourceNotFoundException;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsDataSource;
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
    private final VectorEmbeddingService embeddingService;
    private final VectorEmbeddingDataRepository embeddingDataRepository;

    public VectorEmbeddingsController(List<VectorEmbeddingsDataSource> dataSources,
                                      VectorEmbeddingService embeddingService,
                                      VectorEmbeddingDataRepository embeddingDataRepository) {
        this.dataSources = dataSources;
        this.embeddingService = embeddingService;
        this.embeddingDataRepository = embeddingDataRepository;
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
                            @RequestParam(name = "batchSize", required = false, defaultValue = "500")
                            int batchSize,
                            @RequestParam(name = "skip", required = false, defaultValue = "0")
                            int skip,
                            @RequestParam(name = "rateLimitDelay", required = false, defaultValue = "5000")
                            int rateLimitDelay) {
        var dataSource = embeddingsDataSource(dataType);
        Thread.startVirtualThread(() -> {
            try {
                embeddingService.generateAndSaveEmbeddings(model, dataSource, batchSize, skip, rateLimitDelay);
            } catch (Exception e) {
                logger.error("Problem while generating vector embeddings for {} data type, using {} model:",
                    dataType, model, e);
            }
        });
    }

    @PostMapping("/search")
    VectorEmbeddingsSearchResult search(@RequestBody SearchRequest request) {
        var result = Extensions.timed(() -> embeddingService.search(
            new VectorEmbeddingTableKey(request.model, request.dataSource),
            request.input, request.limit));
        return new VectorEmbeddingsSearchResult(result.time(), result.result());
    }

    @PostMapping("/raw-search")
    VectorEmbeddingsSearchResult rawSearch(@RequestBody RawSearchRequest request) {
        var result = Extensions.timed(() -> embeddingService.rawSearch(
            new VectorEmbeddingTableKey(request.model, request.dataSource),
            request.input, request.limit));
        return new VectorEmbeddingsSearchResult(result.time(), result.result());
    }

    @GetMapping("/data/{id}")
    VectorEmbeddingData embeddingData(@PathVariable("id") String id) {
        return embeddingDataRepository.ofId(id, Object.class)
            .orElseThrow(() -> ResourceNotFoundException.ofType("VectorEmbeddingData", id));
    }

    @PostMapping("/similar-to-embedding-search")
    VectorEmbeddingsSearchResult similarToEmbeddingSearch(@RequestBody SimilarToVectorSearchRequest request) {
        var result = Extensions.timed(() -> embeddingService.similarToEmbeddingSearch(
            new VectorEmbeddingTableKey(request.model, request.dataSource),
            request.embeddingId, 10));
        return new VectorEmbeddingsSearchResult(result.time(), result.result());
    }

    public record LoadDataRequest(String type, String path) {
    }

    public record SearchRequest(String input, VectorEmbeddingModel model, String dataSource, Integer limit) {
        public SearchRequest {
            limit = limit == null ? 5 : limit;
        }
    }

    public record RawSearchRequest(List<Float> input, VectorEmbeddingModel model, String dataSource, Integer limit) {
        public RawSearchRequest {
            limit = limit == null ? 5 : limit;
        }
    }

    public record SimilarToVectorSearchRequest(UUID embeddingId, VectorEmbeddingModel model, String dataSource) {
    }

    public static class DataTypeNotSupportedException extends RuntimeException {
        public DataTypeNotSupportedException(String type) {
            super("%s type is not supported".formatted(type));
        }
    }
}
