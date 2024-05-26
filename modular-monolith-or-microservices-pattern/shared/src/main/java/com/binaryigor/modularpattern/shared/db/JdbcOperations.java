package com.binaryigor.modularpattern.shared.db;

import java.sql.ResultSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JdbcOperations {

    public static String bulkInsertSql(String table, List<String> columns, int rows) {
        var argPlaceholders = Stream.generate(() -> columns.stream().map(e -> "?")
                .collect(Collectors.joining(", ", "(", ")")))
            .limit(rows)
            .collect(Collectors.joining(",\n"));
        return """
            INSERT INTO %s (%s) VALUES %s
            """.formatted(table, String.join(", ", columns), argPlaceholders);
    }

    public static UUID getUUID(ResultSet r, String column) {
        try {
            return UUID.fromString(r.getString(column));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
