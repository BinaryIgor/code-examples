package com.binaryigor.modularpattern.shared.leader;

import org.springframework.jdbc.core.simple.JdbcClient;

public class PostgresAdvisoryLockLeaderAwareness implements LeaderAwareness {

    private final JdbcClient jdbcClient;
    private final int lockId;

    public PostgresAdvisoryLockLeaderAwareness(JdbcClient jdbcClient, int lockId) {
        this.jdbcClient = jdbcClient;
        this.lockId = lockId;
    }

    @Override
    public void executeIfLeader(Runnable runnable) {
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
    }
}
