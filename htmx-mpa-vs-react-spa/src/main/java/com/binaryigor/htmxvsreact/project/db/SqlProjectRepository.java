package com.binaryigor.htmxvsreact.project.db;

import com.binaryigor.htmxvsreact.project.Project;
import com.binaryigor.htmxvsreact.project.ProjectRepository;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Clock;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SqlProjectRepository implements ProjectRepository {

    private final JdbcClient jdbcClient;
    private final Clock clock;

    public SqlProjectRepository(JdbcClient jdbcClient, Clock clock) {
        this.jdbcClient = jdbcClient;
        this.clock = clock;
    }

    @Override
    public List<Project> userProjects(UUID userId) {
        return jdbcClient.sql("SELECT * FROM project WHERE owner_id = ?")
            .params(userId)
            .query((r, $) -> toProject(r))
            .list();
    }

    private Project toProject(ResultSet r) throws SQLException {
        return new Project(UUID.fromString(r.getString("id")),
            r.getString("name"),
            UUID.fromString(r.getString("owner_id")));
    }

    @Override
    public List<Project> ofNames(Collection<String> names) {
        return jdbcClient.sql("SELECT * FROM project WHERE name IN (:names)")
            .param("names", names)
            .query((r, $) -> toProject(r))
            .list();
    }

    @Override
    public Optional<Project> ofName(String name) {
        return jdbcClient.sql("SELECT * FROM project WHERE name = ?")
            .params(name)
            .query((r, $) -> toProject(r))
            .optional();
    }

    @Override
    public Optional<Project> ofId(UUID id) {
        return jdbcClient.sql("SELECT * FROM project WHERE id = ?")
            .params(id)
            .query((r, $) -> toProject(r))
            .optional();
    }

    @Override
    public void save(Project project) {
        var now = clock.instant().toEpochMilli();
        jdbcClient.sql("""
                INSERT INTO project (id, name, owner_id, created_at, updated_at)
                VALUES (:id, :name, :owner_id, :created_at, :updated_at)
                ON CONFLICT (id)
                DO UPDATE
                SET name = EXCLUDED.name,
                    updated_at = EXCLUDED.updated_at
                """)
            .param("id", project.id())
            .param("name", project.name())
            .param("owner_id", project.ownerId())
            .param("created_at", now)
            .param("updated_at", now)
            .update();
    }

    @Override
    public void delete(UUID id) {
        jdbcClient.sql("DELETE FROM project WHERE id = ?")
            .param(id)
            .update();
    }
}
