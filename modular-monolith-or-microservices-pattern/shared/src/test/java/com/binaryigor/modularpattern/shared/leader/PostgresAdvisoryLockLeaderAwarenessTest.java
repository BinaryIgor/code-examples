package com.binaryigor.modularpattern.shared.leader;

import com.binaryigor.modularpattern.shared.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class PostgresAdvisoryLockLeaderAwarenessTest extends IntegrationTest {

    private final int lockId = 99;
    private final PostgresAdvisoryLockLeaderAwareness leaderAwareness = new PostgresAdvisoryLockLeaderAwareness(jdbcClient(), lockId);

    @Test
    void executesOnlyIfLockAvailable() {
        var executed = new AtomicInteger();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.execute(() -> {
                leaderAwareness.executeIfLeader(() -> {
                    await(100);
                    executed.incrementAndGet();
                });
            });
        }

        Assertions.assertThat(executed.get()).isOne();
    }

    private void await(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
