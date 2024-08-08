package com.binaryigor.simplewebanalytics.db;

import com.binaryigor.simplewebanalytics.core.AnalyticsEvent;
import com.binaryigor.simplewebanalytics.core.AnalyticsEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SqlAnalyticsEventRepository implements AnalyticsEventRepository {

    private final JdbcClient jdbcClient;
    private final ObjectMapper objectMapper;

    public SqlAnalyticsEventRepository(JdbcClient jdbcClient,
                                       ObjectMapper objectMapper) {
        this.jdbcClient = jdbcClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public void create(Collection<AnalyticsEvent> events) {
        if (events.isEmpty()) {
            return;
        }

        var batchInsert = """
            INSERT INTO analytics_event
            (timestamp, ip, device_id, user_id, url, browser, platform, device, type, data)""";

        var argPlaceholders = events.stream().map(e -> "(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")
            .collect(Collectors.joining(",\n"));
        var argValues = events.stream()
            .flatMap(e -> Stream.of(Timestamp.from(e.timestamp()), e.ip(), e.deviceId(), e.userId(),
                e.url(), e.browser(), e.platform(), e.device(), e.type(), toJsonb(e.data())))
            .toList();

        jdbcClient.sql(batchInsert + " VALUES " + argPlaceholders)
            .params(argValues)
            .update();
    }

    private PGobject toJsonb(Object data) {
        if (data == null) {
            return null;
        }

        try {
            var pgObject = new PGobject();
            pgObject.setType("jsonb");
            pgObject.setValue(objectMapper.writeValueAsString(data));
            return pgObject;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public List<AnalyticsEvent> all() {
        return jdbcClient.sql("SELECT * FROM analytics_event")
            .query((r, n) -> rowToAnalyticsEvent(r))
            .list();
    }

    private AnalyticsEvent rowToAnalyticsEvent(ResultSet row) {
        try {
            return new AnalyticsEvent(
                row.getTimestamp("timestamp").toInstant(),
                row.getString("ip"),
                row.getObject("device_id", UUID.class),
                row.getObject("user_id", UUID.class),
                row.getString("url"),
                row.getString("browser"),
                row.getString("platform"),
                row.getString("device"),
                row.getString("type"),
                fromJsonb(row.getObject("data", PGobject.class))
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Object fromJsonb(PGobject jsonb) throws Exception {
        if (jsonb.getValue() == null) {
            return null;
        }
        var type = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class, Object.class);
        return objectMapper.readValue(jsonb.getValue(), type);
    }
}
