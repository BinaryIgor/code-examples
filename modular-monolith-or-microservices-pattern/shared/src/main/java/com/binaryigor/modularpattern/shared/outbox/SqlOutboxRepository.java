package com.binaryigor.modularpattern.shared.outbox;

import com.binaryigor.modularpattern.shared.db.JdbcOperations;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SqlOutboxRepository implements OutboxRepository {

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;
    private final String tableName;

    public SqlOutboxRepository(JdbcClient jdbcClient,
                               ObjectMapper objectMapper,
                               String tableName) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
        this.tableName = tableName;
    }

    public SqlOutboxRepository(JdbcClient jdbcClient, ObjectMapper objectMapper) {
        this(jdbcClient, objectMapper, "outbox_message");
    }

    @Override
    public void save(OutboxMessage message) {
        save(List.of(message));
    }

    @Override
    public void save(Collection<OutboxMessage> messages) {
        var values = messages.stream().flatMap(m -> Stream.of(m.id(), toJsonb(m))).toList();

        var sql = JdbcOperations.bulkInsertSql(tableName, List.of("id", "message"), messages.size());

        jdbcClient.sql(sql)
            .params(values)
            .update();

    }

    @Override
    public List<OutboxMessage> toSend(Instant now, int limit) {
        return jdbcClient.sql("SELECT * FROM %s WHERE send_at <= ? ORDER BY created_at LIMIT %d".formatted(tableName, limit))
            .params(Timestamp.from(now))
            .query(RawJsonOutboxMessage.class)
            .list()
            .stream()
            .map(m -> m.toOutboxMessage(objectMapper))
            .toList();
    }

    @Override
    public List<OutboxMessage> all() {
        return jdbcClient.sql("SELECT * FROM " + tableName)
            .query(RawJsonOutboxMessage.class)
            .list()
            .stream()
            .map(m -> m.toOutboxMessage(objectMapper))
            .toList();
    }

    @Override
    public void delete(Collection<UUID> ids) {
        if (!ids.isEmpty()) {
            jdbcClient.sql("DELETE FROM %s WHERE id IN (:ids)".formatted(tableName))
                .param("ids", ids)
                .update();
        }
    }

    @Override
    public void updateSendAt(Map<UUID, Instant> idsSendAt) {
        if (idsSendAt.isEmpty()) {
            return;
        }
        var argPlaceholders = Stream.generate(() -> "?").limit(idsSendAt.size())
            .collect(Collectors.joining(","));

        var idArgs = idsSendAt.keySet().stream().toList();
        var sendAtArgs = idsSendAt.values().stream().map(Timestamp::from).toList();

        var sql = """
            UPDATE %s SET send_at = update_table.send_at::TIMESTAMP
            FROM
            (SELECT UNNEST(ARRAY[%s]) AS id,
                    UNNEST(ARRAY[%s]) AS send_at) AS update_table
            WHERE %s.id = update_table.id""".formatted(tableName, argPlaceholders, argPlaceholders, tableName);

        jdbcClient.sql(sql)
            .params(idArgs)
            .params(sendAtArgs)
            .update();
    }

    private PGobject toJsonb(OutboxMessage message) {
        try {
            var messageData = message.message();
            var json = objectMapper.writeValueAsString(messageData);
            var jsonMessage = objectMapper.writeValueAsString(new JsonMessage(messageData.getClass().getName(), json));

            var jsonObject = new PGobject();
            jsonObject.setType("jsonb");
            jsonObject.setValue(jsonMessage);
            return jsonObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private record RawJsonOutboxMessage(UUID id, String message) {

        OutboxMessage toOutboxMessage(ObjectMapper objectMapper) {
            try {
                var jsonMessage = objectMapper.readValue(message, JsonMessage.class);
                var dataClass = Class.forName(jsonMessage.type());
                var data = objectMapper.readValue(jsonMessage.value(), dataClass);
                return new OutboxMessage(id, data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private record JsonMessage(String type, String value) {
    }
}
