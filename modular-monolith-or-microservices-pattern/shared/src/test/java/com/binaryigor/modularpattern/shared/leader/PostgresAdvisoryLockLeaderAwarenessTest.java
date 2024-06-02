package com.binaryigor.modularpattern.shared.leader;

import com.binaryigor.modularpattern.shared.IntegrationTest;
import com.binaryigor.modularpattern.shared.db.SpringTransactions;
import com.binaryigor.modularpattern.shared.db.Transactions;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PostgresAdvisoryLockLeaderAwarenessTest extends IntegrationTest {

    private final int lockId = 99;

    private final PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
    private final Transactions transactions = new SpringTransactions(new TransactionTemplate(transactionManager));

    private final PostgresAdvisoryLockLeaderAwareness leaderAwareness = new PostgresAdvisoryLockLeaderAwareness(jdbcClient, transactions, lockId);

    @Test
    void executesOnlyIfLockAvailable() {
        var executed = new AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10; i++) {
                executor.execute(() -> {
                    leaderAwareness.executeIfLeader(() -> {
                        await(100);
                        executed.incrementAndGet();
                    });
                });
            }
        }

        Assertions.assertThat(executed.get()).isOne();
    }

    @Test
    void executesAfterUnlocking() {
        var executed = new AtomicInteger();

        leaderAwareness.executeIfLeader(executed::incrementAndGet);

        leaderAwareness.executeIfLeader(executed::incrementAndGet);

        Assertions.assertThat(executed.get()).isEqualTo(2);
    }

    private void await(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
