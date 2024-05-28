package com.binaryigor.modularpattern.user.infra;

import com.binaryigor.modularpattern.user.domain.exception.OptimisticLockException;
import com.binaryigor.modularpattern.user.domain.User;
import com.binaryigor.modularpattern.user.domain.UserRepository;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class SqlUserRepository implements UserRepository {

    private static final String TABLE = "\"user\"";
    private final JdbcClient jdbcClient;

    public SqlUserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public User save(User user) {
        if (user.version() == null) {
            return jdbcClient.sql("""
                    INSERT INTO %s (id, email, name, version) VALUES (?, ?, ?, ?)
                    RETURNING id, email, name, version
                    """.formatted(TABLE))
                .params(user.id(), user.email(), user.name(), 1)
                .query(User.class)
                .single();
        }

        return jdbcClient.sql("""
                UPDATE %s
                SET email = :email,
                    name = :name,
                    version = :version + 1
                WHERE id = :id AND version = :version
                RETURNING id, email, name, version
                """.formatted(TABLE))
            .param("id", user.id())
            .param("email", user.email())
            .param("name", user.name())
            .param("version", user.version())
            .query(User.class)
            .optional()
            .orElseThrow(() -> new OptimisticLockException("Outdated user version"));
    }

    @Override
    public Optional<User> ofId(UUID id) {
        return userOf("id", id);
    }

    @Override
    public Optional<User> ofEmail(String email) {
        return userOf("email", email);
    }

    @Override
    public Stream<User> allUsers() {
        return jdbcClient.sql("SELECT id, name, email, version FROM %s".formatted(TABLE))
            .query(User.class)
            .stream();
    }

    private Optional<User> userOf(String column, Object value) {
        return jdbcClient.sql("SELECT id, email, name, version FROM %s WHERE %s = ?".formatted(TABLE, column))
            .param(value)
            .query(User.class)
            .optional();
    }
}
