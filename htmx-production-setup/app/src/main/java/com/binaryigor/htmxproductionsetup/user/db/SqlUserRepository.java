package com.binaryigor.htmxproductionsetup.user.db;

import com.binaryigor.htmxproductionsetup.user.domain.User;
import com.binaryigor.htmxproductionsetup.user.domain.UserRepository;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class SqlUserRepository implements UserRepository {

    private static final String USER_TABLE = "\"user\".\"user\"";
    private final JdbcClient jdbcClient;

    public SqlUserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(User user) {
        jdbcClient.sql("""
                        INSERT INTO %s (id, email, name, password, language)
                        VALUES (:id, :email, :name, :password, :language)
                        ON CONFLICT (id) DO UPDATE
                        SET name = EXCLUDED.name,
                            email = EXCLUDED.email,
                            password = EXCLUDED.password,
                            language = EXCLUDED.language""".formatted(USER_TABLE))
                .param("id", user.id())
                .param("email", user.email())
                .param("name", user.name())
                .param("password", user.password())
                .param("language", user.language().name())
                .update();
    }

    @Override
    public Optional<User> ofEmail(String email) {
        return jdbcClient.sql("SELECT * FROM %s WHERE email = ?".formatted(USER_TABLE))
                .param(email)
                .query(User.class)
                .optional();
    }

    @Override
    public Optional<User> ofId(UUID id) {
        return jdbcClient.sql("SELECT * FROM %s WHERE id = ?".formatted(USER_TABLE))
                .param(id)
                .query(User.class)
                .optional();
    }
}
