package com.binaryigor.modularpattern.shared.leader;

import com.binaryigor.modularpattern.shared.db.Transactions;
import org.springframework.jdbc.core.simple.JdbcClient;

public class PostgresAdvisoryLockLeaderAwareness implements LeaderAwareness {

    private final JdbcClient jdbcClient;
    private final Transactions transactions;
    private final int lockId;

    public PostgresAdvisoryLockLeaderAwareness(JdbcClient jdbcClient,
                                               Transactions transactions,
                                               int lockId) {
        this.jdbcClient = jdbcClient;
        this.transactions = transactions;
        this.lockId = lockId;
    }

    @Override
    public void executeIfLeader(Runnable runnable) {
        transactions.execute(() -> {
            var locked = false;
            try {
                locked = jdbcClient.sql("SELECT pg_try_advisory_lock(%d)".formatted(lockId))
                    .query(Boolean.class)
                    .single();
                if (locked) {
                    runnable.run();
                }
            } finally {
                if (locked) {
                    jdbcClient.sql("SELECT pg_advisory_unlock(%d)".formatted(lockId))
                        .query(Boolean.class)
                        .single();
                }
            }
        });
    }
}
