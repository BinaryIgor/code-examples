package com.binaryigor.vembeddingswithpostgres.embeddings;

import com.binaryigor.vembeddingswithpostgres.data.VectorEmbeddingInputData;
import com.binaryigor.vembeddingswithpostgres.generator.VectorEmbeddingsGenerator;
import com.binaryigor.vembeddingswithpostgres.shared.SizedStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class VectorEmbeddingService {

    private static final Logger logger = LoggerFactory.getLogger(VectorEmbeddingService.class);
    private final VectorEmbeddingRepository repository;
    private final List<VectorEmbeddingsGenerator> generators;

    public VectorEmbeddingService(VectorEmbeddingRepository repository,
                                  List<VectorEmbeddingsGenerator> generators) {
        this.repository = repository;
        this.generators = generators;
    }

    public void generateAndSaveEmbeddings(VectorEmbeddingModel model,
                                          SizedStream<VectorEmbeddingInputData> data,
                                          int batchSize,
                                          int skip,
                                          int rateLimitDelay) {
        logger.info("About to generate {} vector embeddings, using {} model and skipping first {} items",
            data.size(), model, skip);

        var generateEmbeddingInputs = new LinkedList<VectorEmbeddingInputData>();

        var idx = new AtomicInteger();

        try (var stream = data.stream()) {
            stream.forEach(d -> {
                var idxValue = idx.incrementAndGet();
                if (skip >= idxValue) {
                    return;
                }

                generateEmbeddingInputs.add(d);

                if (generateEmbeddingInputs.size() >= batchSize) {
                    logger.info("Generating and saving {}/{} vector embeddings...", idxValue, data.size());
                    var generatedEmbeddings = generateVectorEmbeddings(model, generateEmbeddingInputs);
                    repository.save(model, generatedEmbeddings);
                    generateEmbeddingInputs.clear();
                    rateLimitDelay(rateLimitDelay);
                }
            });
        }

        if (!generateEmbeddingInputs.isEmpty()) {
            logger.info("Saving last vector embeddings batch...");
            var generatedEmbeddings = generateVectorEmbeddings(model, generateEmbeddingInputs);
            repository.save(model, generatedEmbeddings);
        }

        logger.info("{} vector embeddings generated and saved!", data.size());
    }

    private List<Float> generateVectorEmbedding(VectorEmbeddingModel model, String input) {
        var generator = generator(model);
        return generator.generate(model, input);
    }

    private VectorEmbeddingsGenerator generator(VectorEmbeddingModel model) {
        return generators.stream()
            .filter(g -> g.supports(model))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("There is no generator for %s model".formatted(model)));
    }

    private List<VectorEmbedding> generateVectorEmbeddings(VectorEmbeddingModel model, List<VectorEmbeddingInputData> inputs) {
        var generator = generator(model);
        var generated = generator.generateBatch(model, inputs);
        return generated.entrySet().stream()
            .map(e -> {
                var input = e.getKey();
                var embedding = e.getValue();
                return new VectorEmbedding(input.id(), embedding, input.data());
            })
            .toList();
    }

    private void rateLimitDelay() {
        rateLimitDelay(20_000);
    }

    private void rateLimitDelay(long delay) {
        try {
            Thread.sleep(delay);
        } catch (Exception e) {

        }
    }

    public List<VectorEmbeddingSearchResult> search(VectorEmbeddingModel model,
                                                    String input,
                                                    int limit) {
        var embeddingInput = generateVectorEmbedding(model, input);
        return repository.mostSimilar(model, embeddingInput, limit);
    }

    public List<VectorEmbeddingSearchResult> rawSearch(VectorEmbeddingModel model,
                                                       List<Float> input,
                                                       int limit) {
        return repository.mostSimilar(model, input, limit);
    }

    public List<VectorEmbeddingSearchResult> similarToEmbeddingSearch(VectorEmbeddingModel model,
                                                                      UUID embeddingId,
                                                                      int limit) {
        var embedding = repository.ofId(model, embeddingId).orElseThrow();
        return repository.mostSimilar(model, embedding.embedding(), limit);
    }

    private record GenerateEmbeddingInput(String id, String input) {
    }
}
