package com.binaryigor.modularpattern.project.infra;

import com.binaryigor.modularpattern.project.domain.Project;
import com.binaryigor.modularpattern.project.domain.ProjectRepository;
import com.binaryigor.modularpattern.shared.db.JdbcOperations;
import com.binaryigor.modularpattern.shared.db.Transactions;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class SqlProjectRepository implements ProjectRepository {

    private final JdbcClient jdbcClient;
    private final Transactions transactions;

    public SqlProjectRepository(JdbcClient jdbcClient, Transactions transactions) {
        this.jdbcClient = jdbcClient;
        this.transactions = transactions;
    }

    @Override
    public void save(Project project) {
        transactions.execute(() -> {
            jdbcClient.sql("""
                    INSERT INTO project (id, namespace, name, description) VALUES (?, ?, ?, ?)
                    ON CONFLICT (id) DO UPDATE
                      SET namespace = EXCLUDED.namespace,
                          name = EXCLUDED.name,
                          description = EXCLUDED.description
                    """)
                .params(project.id(), project.namespace(), project.name(), project.description())
                .update();

            jdbcClient.sql("DELETE FROM project_user WHERE project_id = ?")
                .param(project.id())
                .update();

            if (!project.userIds().isEmpty()) {
                var projectUserIds = project.userIds().stream()
                    .flatMap(uid -> Stream.of(project.id(), uid))
                    .toList();

                var sql = JdbcOperations.bulkInsertSql("project_user",
                    List.of("project_id", "user_id"),
                    project.userIds().size());

                jdbcClient.sql(sql)
                    .params(projectUserIds)
                    .update();
            }
        });
    }

    @Override
    public Optional<Project> ofId(UUID id) {
        return jdbcClient.sql("SELECT * FROM project WHERE id = ?")
            .params(id)
            .query(ProjectWithoutUsersProjection.class)
            .optional()
            .map(p -> {
                var userIds = jdbcClient.sql("SELECT user_id FROM project_user WHERE project_id = ?")
                    .params(id)
                    .query((r, n) -> JdbcOperations.getUUID(r, "user_id"))
                    .list();
                return p.toProject(userIds);
            });
    }

    @Override
    public List<Project> allOfNamespace(String namespace) {
        // TODO: implement!
        return List.of();
    }

    record ProjectWithoutUsersProjection(UUID id, String namespace, String name, String description) {

        public Project toProject(List<UUID> userIds) {
            return new Project(id, namespace, name, description, userIds);
        }
    }
}
