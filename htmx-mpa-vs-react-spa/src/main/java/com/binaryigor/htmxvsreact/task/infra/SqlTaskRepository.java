package com.binaryigor.htmxvsreact.task.infra;

import com.binaryigor.htmxvsreact.shared.contracts.TaskClient;
import com.binaryigor.htmxvsreact.task.domain.Task;
import com.binaryigor.htmxvsreact.task.domain.TaskRepository;
import com.binaryigor.htmxvsreact.task.domain.TaskStatus;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

public class SqlTaskRepository implements TaskRepository, TaskClient {

    private final JdbcClient jdbcClient;

    public SqlTaskRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(Task task) {
        jdbcClient.sql("""
                INSERT INTO task (id, name, project_id, owner_id, status) VALUES (:id, :name, :project_id, :owner_id, :status)
                ON CONFLICT (id)
                DO UPDATE
                SET name = EXCLUDED.name,
                    project_id = EXCLUDED.project_id,
                    owner_id = EXCLUDED.owner_id,
                    status = EXCLUDED.status
                """)
            .param("id", task.id())
            .param("name", task.name())
            .param("project_id", task.projectId())
            .param("owner_id", task.ownerId())
            .param("status", task.status().name())
            .update();
    }

    @Override
    public Optional<Task> ofId(UUID id) {
        return jdbcClient.sql("SELECT * FROM task WHERE id = ?")
            .params(id)
            .query((r, $) -> toTask(r))
            .optional();
    }

    private Task toTask(ResultSet r) throws SQLException {
        return new Task(UUID.fromString(r.getString("id")),
            r.getString("name"),
            UUID.fromString(r.getString("project_id")),
            UUID.fromString(r.getString("owner_id")),
            TaskStatus.valueOf(r.getString("status")));
    }

    @Override
    public List<Task> of(Collection<UUID> projectIds, Collection<TaskStatus> statuses) {
        var sql = "SELECT * FROM task WHERE project_id IN (:project_ids)";
        if (statuses != null && !statuses.isEmpty()) {
            sql += " AND status IN (:statuses)";
        }
        return jdbcClient.sql(sql)
            .param("project_ids", projectIds)
            .param("statuses", statuses)
            .query((r, $) -> toTask(r))
            .list();
    }

    @Override
    public Map<UUID, Integer> tasksCountOfProjects(Collection<UUID> projectIds) {
        return jdbcClient.sql("SELECT project_id, COUNT(*) AS tasks FROM task WHERE project_id IN (:project_ids) GROUP BY project_id")
            .param("project_ids", projectIds)
            .query((r, n) -> Map.entry(UUID.fromString(r.getString("project_id")), r.getInt("tasks")))
            .list()
            .stream()
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public void delete(UUID id) {
        jdbcClient.sql("DELETE FROM task WHERE id = ?")
            .param(id)
            .update();
    }
}
