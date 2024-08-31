package com.binaryigor.vembeddingswithpostgres;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class VectorEmbeddingsService {

    private static final Logger logger = LoggerFactory.getLogger(VectorEmbeddingsService.class);
    private final VectorEmbeddingsRepository repository;
    private final List<VectorEmbeddingsGenerator> generators;

    public VectorEmbeddingsService(VectorEmbeddingsRepository repository,
                                   List<VectorEmbeddingsGenerator> generators) {
        this.repository = repository;
        this.generators = generators;
    }

    public void generateAndSaveRandomEmbeddings(VectorEmbeddingModel model, int size, int batchSize) {
        try {
            var batch = new LinkedList<VectorEmbedding>();

            for (int i = 0; i < size; i++) {
                var input = UUID.randomUUID().toString();
                var embedding = generateVectorEmbedding(model, input);
                batch.add(new VectorEmbedding(UUID.randomUUID(), embedding, input));

                if (batch.size() >= batchSize) {
                    logger.info("Saving {}/{} vector embeddings...", i, size);
                    repository.save(model, batch);
                    batch.clear();
                }
            }

            if (!batch.isEmpty()) {
                logger.info("Saving last vector embeddings...");
                repository.save(model, batch);
                batch.clear();
            }

            logger.info("{} random vector embeddings generated and saved!", size);
        } catch (Exception e) {
            logger.error("Problem while generating and saving random vector embeddings:", e);
        }
    }

    private List<Float> generateVectorEmbedding(VectorEmbeddingModel model, String input) {
        var generator = generators.stream()
            .filter(g -> g.supports(model))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("There is no generator for %s model".formatted(model)));

        return generator.generate(model, input);
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
}
