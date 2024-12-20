package com.binaryigor.single.app.account;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Repository
public class AccountRepository {

    private final JdbcClient jdbcClient;

    public AccountRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Optional<Account> accountById(UUID id) {
        return jdbcClient.sql("SELECT * FROM account WHERE id = ?")
            .param(id)
            .query((r, n) -> fromRow(r))
            .optional();
    }

    private Account fromRow(ResultSet r) throws SQLException {
        return new Account(
            UUID.fromString(r.getString("id")),
            r.getString("name"),
            r.getString("email"),
            Instant.ofEpochMilli(r.getLong("created_at")),
            r.getLong("version"));
    }

    public List<UUID> accountIds(int limit, List<UUID> toSkipIds) {
        if (toSkipIds.isEmpty()) {
            return jdbcClient.sql("SELECT id FROM account limit " + limit).query(UUID.class).list();
        }
        return jdbcClient.sql("SELECT id FROM account WHERE id NOT IN (:ids) LIMIT :limit")
            .param("ids", toSkipIds)
            .param("limit", limit)
            .query(UUID.class)
            .list();
    }

    public List<Account> accountsByName(String name) {
        return jdbcClient.sql("SELECT * FROM account WHERE name = ?")
            .param(name)
            .query((r, n) -> fromRow(r))
            .list();
    }

    public void create(List<Account> accounts) {
        create(accounts, 500);
    }

    public void create(List<Account> accounts, int batchSize) {
        if (accounts.isEmpty()) {
            return;
        }

        var batches = toInsertBatches(accounts, batchSize);

        for (var batch : batches) {
            var sqlWithoutArgs = "INSERT INTO account (id, name, email, created_at, version) VALUES ";

            var argsPlaceholders = IntStream.range(0, batch.size())
                .mapToObj(i -> "(?, ?, ?, ?, ?)")
                .collect(Collectors.joining(",\n"));

            var sql = sqlWithoutArgs + argsPlaceholders;

            var params = batch.stream()
                .flatMap(a -> Stream.of(a.id(), a.name(), a.email(), a.createdAt().toEpochMilli(), a.version()))
                .toList();

            jdbcClient.sql(sql).params(params).update();
        }
    }

    private Collection<List<Account>> toInsertBatches(List<Account> accounts, int batchSize) {
        var index = new AtomicInteger();
        return accounts.stream()
            .collect(Collectors.groupingBy(i -> index.getAndIncrement() / batchSize))
            .values();
    }

    public void delete(UUID id) {
        jdbcClient.sql("DELETE FROM account WHERE id = ?")
            .param(id)
            .update();
    }
}
