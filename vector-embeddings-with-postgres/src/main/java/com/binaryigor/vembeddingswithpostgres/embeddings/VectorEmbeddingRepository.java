package com.binaryigor.vembeddingswithpostgres.embeddings;

import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingModel;
import com.binaryigor.vembeddingswithpostgres.shared.VectorEmbeddingsSupportedDataSources;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VectorEmbeddingRepository {

    private final Map<VectorEmbeddingTableKey, String> tablesByModelAndDataSource;
    private final Map<VectorEmbeddingTableKey, Integer> tablesIVFFlatIndexProbesParam = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(VectorEmbeddingRepository.class);
    private final JdbcClient jdbcClient;
    private final TransactionTemplate transactionTemplate;

    public VectorEmbeddingRepository(JdbcClient jdbcClient,
                                     PlatformTransactionManager transactionManager,
                                     VectorEmbeddingsSupportedDataSources dataSources) {
        this.jdbcClient = jdbcClient;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
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

            var ivfflatIndexListsParam = determineIVFFlatListsParam(countAllOf(key));
            tablesIVFFlatIndexProbesParam.put(key, determineIVFFlatIndexProbesParam(ivfflatIndexListsParam));
        });
    }

    private int determineIVFFlatIndexProbesParam(int listsParam) {
        if (listsParam < 100) {
            return 1;
        }
        if (listsParam < 500) {
            return 3;
        }
        return 5;
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

    public Optional<VectorEmbedding> ofId(VectorEmbeddingTableKey tableKey, String id) {
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
        return transactionTemplate.execute(t -> {
            var probesParam = tablesIVFFlatIndexProbesParam.getOrDefault(tableKey, 1);
            if (probesParam > 1) {
                jdbcClient.sql("SET LOCAL ivfflat.probes=" + probesParam)
                    .update();
            }
            return jdbcClient.sql("SELECT id, embedding_input, embedding <-> ? AS distance FROM %s ORDER BY embedding <-> ? LIMIT ?"
                    .formatted(embeddingTable(tableKey)))
                .params(pgVector, pgVector, limit)
                .query((r, n) -> new VectorEmbeddingSearchResult(
                    r.getString("id"),
                    r.getString("embedding_input"),
                    r.getFloat("distance")))
                .list();
        });
    }

    public List<VectorEmbedding> allOf(VectorEmbeddingTableKey tableKey) {
        return jdbcClient.sql("SELECT id, embedding, embedding_input FROM %s".formatted(embeddingTable(tableKey)))
            .query((r, n) -> vectorEmbedding(r))
            .list();
    }

    public int countAllOf(VectorEmbeddingTableKey tableKey) {
        return jdbcClient.sql("SELECT COUNT(*) FROM %s".formatted(embeddingTable(tableKey)))
            .query(Integer.class)
            .optional()
            .orElse(0);
    }

    public void reindexIVFFlat(VectorEmbeddingTableKey tableKey) {
        var table = embeddingTable(tableKey);

        var tableCount = countAllOf(tableKey);
        var lists = determineIVFFlatListsParam(tableCount);

        var indexName = table + "_ivfflat";
        var oldIndexName = indexName + "_old";

        transactionTemplate.execute(t -> {
            var maintenanceWorkMem = jdbcClient.sql("SHOW maintenance_work_mem").query(String.class).single();

            // vector indexes take more resources to create
            jdbcClient.sql("SET maintenance_work_mem='1GB'")
                .update();

            jdbcClient.sql("ALTER INDEX IF EXISTS %s RENAME TO %s".formatted(indexName, oldIndexName))
                .update();

            jdbcClient.sql("CREATE INDEX %s ON %s USING ivfflat (embedding vector_l2_ops) WITH (lists=%d)"
                    .formatted(indexName, table, lists))
                .update();

            jdbcClient.sql("DROP INDEX IF EXISTS " + oldIndexName)
                .update();

            jdbcClient.sql("SET maintenance_work_mem='%s'".formatted(maintenanceWorkMem))
                .update();

            return null;
        });

        tablesIVFFlatIndexProbesParam.put(tableKey, determineIVFFlatIndexProbesParam(lists));
    }

    // recommended params reference: https://github.com/pgvector/pgvector?tab=readme-ov-file#ivfflat
    private int determineIVFFlatListsParam(int tableSize) {
        int lists;
        if (tableSize > 1_000_000) {
            lists = (int) Math.sqrt(tableSize);
        } else if (tableSize > 10_000) {
            lists = tableSize / 1000;
        } else if (tableSize > 1000) {
            lists = tableSize / 100;
        } else {
            lists = tableSize / 10;
        }
        return lists;
    }

    public void reindexHNSW(VectorEmbeddingTableKey tableKey) {
        var table = embeddingTable(tableKey);

        var indexName = table + "_hnsw";
        var oldIndexName = indexName + "_old";

        // default values
        int m = 16;
        int efConstruction = 64;

        transactionTemplate.execute(t -> {
            var maintenanceWorkMem = jdbcClient.sql("SHOW maintenance_work_mem").query(String.class).single();

            // vector indexes take more resources to create
            jdbcClient.sql("SET maintenance_work_mem='8GB'")
                .update();

            jdbcClient.sql("ALTER INDEX IF EXISTS %s RENAME TO %s".formatted(indexName, oldIndexName))
                .update();

            jdbcClient.sql("CREATE INDEX %s ON %s USING hnsw (embedding vector_l2_ops) WITH ( m = %d, ef_construction = %d)"
                    .formatted(indexName, table, m, efConstruction))
                .update();

            jdbcClient.sql("DROP INDEX IF EXISTS " + oldIndexName)
                .update();

            jdbcClient.sql("SET maintenance_work_mem='%s'".formatted(maintenanceWorkMem))
                .update();

            return null;
        });
    }
}
