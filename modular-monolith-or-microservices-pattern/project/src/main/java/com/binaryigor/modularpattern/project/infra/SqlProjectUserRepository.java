package com.binaryigor.modularpattern.project.infra;

import com.binaryigor.modularpattern.project.domain.ProjectUser;
import com.binaryigor.modularpattern.project.domain.ProjectUserRepository;
import com.binaryigor.modularpattern.shared.db.JdbcOperations;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SqlProjectUserRepository implements ProjectUserRepository {

    private static final String TABLE = "\"user\"";

    private final JdbcClient jdbcClient;

    public SqlProjectUserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(Collection<ProjectUser> users) {
        var sql = JdbcOperations.bulkInsertSql(TABLE,
            List.of("id", "email", "name"),
            users.size()) + """
                      ON CONFLICT (id)
                      DO UPDATE
                      SET email = EXCLUDED.email,
                          name = EXCLUDED.name
                      """;

        var values = users.stream()
            .flatMap(u -> Stream.of(u.id(), u.email(), u.name()))
            .toList();

        jdbcClient.sql(sql)
            .params(values)
            .update();
    }

    @Override
    public Map<UUID, ProjectUser> ofIds(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return jdbcClient.sql("SELECT * FROM %s WHERE id IN (:ids)".formatted(TABLE))
            .param("ids", ids)
            .query(ProjectUser.class)
            .list()
            .stream()
            .collect(Collectors.toMap(ProjectUser::id, p -> p));
    }
}
