package com.binaryigor.vembeddingswithpostgres.embeddings;

import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsSupportedDataSources;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VectorEmbeddingRepository {

    private final Map<VectorEmbeddingTableKey, String> tablesByModelAndDataSource;
    private final Logger logger = LoggerFactory.getLogger(VectorEmbeddingRepository.class);
    private final JdbcClient jdbcClient;

    public VectorEmbeddingRepository(JdbcClient jdbcClient,
                                     VectorEmbeddingsSupportedDataSources dataSources) {
        this.jdbcClient = jdbcClient;
        this.tablesByModelAndDataSource = Arrays.stream(VectorEmbeddingModel.values())
            .flatMap(m -> dataSources.get().stream()
                .map(ds -> new VectorEmbeddingTableKey(m, ds)))
            .collect(Collectors.toMap(Function.identity(), this::embeddingTable));
    }

    private String embeddingTable(VectorEmbeddingTableKey key) {
        return "vembedding_" + key.model().name().toLowerCase() + "_" + key.dataSource().toLowerCase();
    }

    public Collection<String> tables() {
        return tablesByModelAndDataSource.values();
    }

    public void initDb() {
        jdbcClient.sql("CREATE EXTENSION IF NOT EXISTS vector").update();

        tablesByModelAndDataSource.forEach((key, table) -> {
            jdbcClient.sql("""
                    CREATE TABLE IF NOT EXISTS %s (
                        id TEXT PRIMARY KEY,
                        embedding VECTOR(%d) NOT NULL,
                        embedding_input TEXT
                    );
                    """.formatted(table, key.model().dimensions))
                .update();
        });
    }

    public void save(VectorEmbeddingTableKey tableKey, VectorEmbedding embedding) {
        save(tableKey, List.of(embedding));
    }

    public void save(VectorEmbeddingTableKey tableKey, List<VectorEmbedding> embeddings) {
        if (embeddings.isEmpty()) {
            return;
        }

        var argPlaceholders = embeddings.stream().map(e -> "(?, ?, ?)").collect(Collectors.joining(",\n"));
        var argValues = embeddings.stream()
            .flatMap(e -> Stream.of(e.id(), pgVector(e.embedding()), e.embeddingInput()))
            .toList();

        var sql = """
            INSERT INTO %s (id, embedding, embedding_input) VALUES %s
            ON CONFLICT (id) DO UPDATE
            SET embedding = EXCLUDED.embedding,
                embedding_input = EXCLUDED.embedding_input
            """.formatted(embeddingTable(tableKey), argPlaceholders);

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

    public Optional<VectorEmbedding> ofId(VectorEmbeddingTableKey tableKey, UUID id) {
        return jdbcClient.sql("SELECT id, embedding, embedding_input FROM %s WHERE id = ?"
                .formatted(embeddingTable(tableKey)))
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

    public List<VectorEmbeddingSearchResult> mostSimilar(VectorEmbeddingTableKey tableKey,
                                                         List<Float> embedding,
                                                         int limit) {
        var pgVector = pgVector(embedding);
        return jdbcClient.sql("SELECT id, embedding_input, embedding <-> ? AS distance FROM %s ORDER BY embedding <-> ? LIMIT ?"
                .formatted(embeddingTable(tableKey)))
            .params(pgVector, pgVector, limit)
            .query((r, n) -> new VectorEmbeddingSearchResult(
                r.getString("id"),
                r.getString("embedding_input"),
                r.getFloat("distance")))
            .list();
    }

    public List<VectorEmbedding> allOf(VectorEmbeddingTableKey tableKey) {
        return jdbcClient.sql("SELECT id, embedding, embedding_input FROM %s".formatted(embeddingTable(tableKey)))
            .query((r, n) -> vectorEmbedding(r))
            .list();
    }
}
