package com.binaryigor.vembeddingswithpostgres.embeddings;

import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VectorEmbeddingsRepository {

    public static final Map<VectorEmbeddingModel, String> TABLES_BY_MODELS = Map.of(
        VectorEmbeddingModel.OPEN_AI_TEXT_3_SMALL, tableOfModel(VectorEmbeddingModel.OPEN_AI_TEXT_3_SMALL)
    );
    private final Logger logger = LoggerFactory.getLogger(VectorEmbeddingsRepository.class);
    private final JdbcClient jdbcClient;

    public VectorEmbeddingsRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    private static String tableOfModel(VectorEmbeddingModel model) {
        return "vector_embedding_" + model.name().toLowerCase();
    }

    public void initDb() {
        jdbcClient.sql("CREATE EXTENSION IF NOT EXISTS vector").update();

        TABLES_BY_MODELS.forEach((m, t) -> {
            jdbcClient.sql("""
                    CREATE TABLE IF NOT EXISTS %s (
                        id TEXT PRIMARY KEY,
                        embedding VECTOR(%d) NOT NULL,
                        embedding_input TEXT
                    );
                    """.formatted(t, m.dimensions))
                .update();
        });
    }

    public void save(VectorEmbeddingModel model, VectorEmbedding embedding) {
        save(model, List.of(embedding));
    }

    public void save(VectorEmbeddingModel model, List<VectorEmbedding> embeddings) {
        if (embeddings.isEmpty()) {
            return;
        }

        var argPlaceholders = embeddings.stream().map(e -> "(?, ?, ?)").collect(Collectors.joining(",\n"));
        var argValues = embeddings.stream()
            .flatMap(e -> Stream.of(e.id(), pgVector(e.embedding()), e.embeddingInput()))
            .toList();

        var sql = "INSERT INTO %s (id, embedding, embedding_input) VALUES".formatted(tableOfModel(model)) + argPlaceholders;

        jdbcClient.sql(sql).params(argValues).update();
    }

    private PGobject pgVector(List<Float> embedding) {
        try {
            var stringEmbedding = embedding.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",", "[", "]"));

            var pgObject = new PGobject();
            pgObject.setType("vector");
            pgObject.setValue(stringEmbedding);
            return pgObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<VectorEmbedding> ofId(VectorEmbeddingModel model, UUID id) {
        return jdbcClient.sql("SELECT id, embedding, embedding_input FROM %s WHERE id = ?"
                .formatted(tableOfModel(model)))
            .param(id)
            .query((r, n) -> vectorEmbedding(r))
            .optional();
    }

    private VectorEmbedding vectorEmbedding(ResultSet result) throws SQLException {
        return new VectorEmbedding(result.getString("id"),
            embedding((PGobject) result.getObject("embedding")),
            result.getString("embedding_input"));
    }

    private List<Float> embedding(PGobject pgVector) {
        try {
            var stringEmbedding = pgVector.getValue();
            return Arrays.stream(stringEmbedding.substring(1, stringEmbedding.length() - 1).split(","))
                .map(Float::parseFloat)
                .toList();
        } catch (Exception e) {
            logger.error("Problem while parsing pgvector type as embedding:", e);
            throw new RuntimeException(e);
        }
    }

    public List<VectorEmbeddingSearchResult> mostSimilar(VectorEmbeddingModel model,
                                                         List<Float> embedding,
                                                         int limit) {
        var pgVector = pgVector(embedding);
        return jdbcClient.sql("SELECT id, embedding_input, embedding <-> ? AS distance FROM %s ORDER BY embedding <-> ? LIMIT ?"
                .formatted(tableOfModel(model)))
            .params(pgVector, pgVector, limit)
            .query((r, n) -> new VectorEmbeddingSearchResult(
                r.getString("id"),
                r.getString("embedding_input"),
                r.getFloat("distance")))
            .list();
    }

    public List<VectorEmbedding> allOfModel(VectorEmbeddingModel model) {
        return jdbcClient.sql("SELECT id, embedding, embedding_input FROM %s".formatted(tableOfModel(model)))
            .query((r, n) -> vectorEmbedding(r))
            .list();
    }
}
