package com.binaryigor.modularpattern.shared.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Collection;
import java.util.List;
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
        var argPlaceholders = messages.stream().map(m -> "(?, ?)").collect(Collectors.joining(", \n"));
        var argValues = messages.stream().flatMap(m -> Stream.of(m.id(), toJsonb(m))).toList();

        jdbcClient.sql("INSERT INTO %s (id, message) VALUES %s".formatted(tableName, argPlaceholders))
                .params(argValues)
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

    @Override
    public List<OutboxMessage> all(int limit) {
        return jdbcClient.sql("SELECT * FROM %s LIMIT %d".formatted(tableName, limit))
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
