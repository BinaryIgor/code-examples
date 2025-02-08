package com.binaryigor.htmxvsreact.user.infra;

import com.binaryigor.htmxvsreact.shared.AppLanguage;
import com.binaryigor.htmxvsreact.user.domain.User;
import com.binaryigor.htmxvsreact.user.domain.UserRepository;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class SqlUserRepository implements UserRepository {

    private final JdbcClient jdbcClient;

    public SqlUserRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void save(User user) {
        jdbcClient.sql("""
                INSERT INTO user (id, email, name, password, language) VALUES (:id, :email, :name, :password, :language)
                ON CONFLICT (id) DO UPDATE
                SET email = EXCLUDED.email,
                    name = EXCLUDED.name,
                    password = EXCLUDED.password,
                    language = EXCLUDED.language""")
            .param("id", user.id().toString())
            .param("email", user.email())
            .param("name", user.name())
            .param("password", user.password())
            .param("language", user.language())
            .update();
    }

    @Override
    public Optional<User> ofId(UUID id) {
        return jdbcClient.sql("SELECT * FROM user WHERE id = ?")
            .param(id)
            .query((r, $) -> toUser(r))
            .optional();
    }

    private User toUser(ResultSet result) throws SQLException {
        return new User(UUID.fromString(result.getString("id")), result.getString("email"),
            result.getString("name"), result.getString("password"),
            AppLanguage.valueOf(result.getString("language")));
    }

    @Override
    public Optional<User> ofEmail(String email) {
        return jdbcClient.sql("SELECT * FROM user WHERE email = ?")
            .param(email)
            .query((r, $) -> toUser(r))
            .optional();
    }
}
