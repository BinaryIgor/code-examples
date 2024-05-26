package com.binaryigor.modularpattern.user.infra;

import com.binaryigor.modularpattern.shared.contracts.UserClient;
import com.binaryigor.modularpattern.shared.contracts.UserView;
import com.binaryigor.modularpattern.user.domain.User;
import com.binaryigor.modularpattern.user.domain.UserRepository;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public class SqlUserRepository implements UserRepository, UserClient {

    private static final String TABLE = "\"user\"";
    private final JdbcClient jdbcClient;

    public SqlUserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(User user) {
        jdbcClient.sql("""
                INSERT INTO %s (id, email, name) VALUES (?, ?, ?)
                ON CONFLICT (id)
                DO UPDATE
                SET email = EXCLUDED.email,
                    name = EXCLUDED.name
                """.formatted(TABLE))
            .params(user.id(), user.email(), user.name())
            .update();
    }

    @Override
    public Optional<User> ofId(UUID id) {
        return userOf("id", id);
    }

    private Optional<User> userOf(String column, Object value) {
        return jdbcClient.sql("SELECT id, email, name FROM %s WHERE %s = ?".formatted(TABLE, column))
            .param(value)
            .query(User.class)
            .optional();
    }

    @Override
    public Optional<User> ofEmail(String email) {
        return userOf("email", email);
    }

    @Override
    public Stream<UserView> allUsers() {
        return jdbcClient.sql("SELECT id, name, email FROM %s".formatted(TABLE))
            .query(UserView.class)
            .stream();
    }
}
