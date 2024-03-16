package com.binaryigor.single.app.account;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

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
                .query(Account.class)
                .optional();
    }

    public void create(List<Account> accounts, int batchSize) {
        if (accounts.isEmpty()) {
            return;
        }

        var batches = toInsertBatches(accounts, batchSize);

        for (var batch : batches) {
            var sqlWithoutArgs = "INSERT INTO account (id, name, email) VALUES ";

            var argsPlaceholders = IntStream.range(0, batch.size())
                    .mapToObj(i -> "(?, ?, ?)")
                    .collect(Collectors.joining(",\n"));

            var sql = sqlWithoutArgs + argsPlaceholders;

            var params = batch.stream()
                    .flatMap(a -> Stream.of(a.id(), a.name(), a.email()))
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
}
