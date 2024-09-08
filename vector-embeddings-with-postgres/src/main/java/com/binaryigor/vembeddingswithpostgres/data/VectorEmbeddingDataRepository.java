package com.binaryigor.vembeddingswithpostgres.data;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VectorEmbeddingDataRepository {

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    public VectorEmbeddingDataRepository(JdbcClient jdbcClient, ObjectMapper objectMapper) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
    }

    public void save(Collection<VectorEmbeddingData> data) {
        if (data.isEmpty()) {
            return;
        }

        var argPlaceholders = data.stream().map(e -> "(?, ?, ?)").collect(Collectors.joining(",\n"));
        var argValues = data.stream()
            .flatMap(e -> Stream.of(e.id(), e.type(), jsonb(e.data())))
            .toList();

        var sql = """
            INSERT INTO vembedding_data (id, type, data) VALUES %s
            ON CONFLICT (id) DO UPDATE
            SET type = EXCLUDED.type,
                data = EXCLUDED.data
            """.formatted(argPlaceholders);

        jdbcClient.sql(sql)
            .params(argValues)
            .update();
    }

    private PGobject jsonb(Object data) {
        try {
            var jsonb = new PGobject();
            jsonb.setType("jsonb");
            jsonb.setValue(objectMapper.writeValueAsString(data));
            return jsonb;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int countOfType(String type) {
        return jdbcClient.sql("SELECT COUNT(*) FROM vembedding_data WHERE type = ?")
            .param(type)
            .query(Integer.class)
            .optional()
            .orElse(0);
    }

    public <T> Stream<VectorEmbeddingData> allOfType(String type, Class<T> dataType) {
        return jdbcClient.sql("SELECT id, type, data FROM vembedding_data WHERE type = ?")
            .param(type)
            .query((r, n) -> vectorEmbeddingData(r, dataType))
            .stream();
    }

    private <T> VectorEmbeddingData vectorEmbeddingData(ResultSet result, Class<T> dataType) throws SQLException {
        return new VectorEmbeddingData(result.getString("id"),
            result.getString("type"),
            data((PGobject) result.getObject("data"), dataType));
    }

    private <T> T data(PGobject jsonb, Class<T> dataType) {
        try {
            return objectMapper.readValue(jsonb.getValue(), dataType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> Optional<VectorEmbeddingData> ofId(String id, Class<T> dataType) {
        return jdbcClient.sql("SELECT id, type, data FROM vembedding_data WHERE id = ?")
            .param(id)
            .query((r, n) -> vectorEmbeddingData(r, dataType))
            .optional();
    }
}
