package com.binaryigor.modularpattern.user.infra;

import com.binaryigor.modularpattern.shared.leader.LeaderAwareness;
import com.binaryigor.modularpattern.shared.outbox.OutboxProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

public class ScheduledOutboxProcessor {

    private final Logger logger = LoggerFactory.getLogger(ScheduledOutboxProcessor.class);
    private final OutboxProcessor outboxProcessor;
    private final LeaderAwareness leaderAwareness;

    public ScheduledOutboxProcessor(OutboxProcessor outboxProcessor,
                                    LeaderAwareness leaderAwareness) {
        this.outboxProcessor = outboxProcessor;
        this.leaderAwareness = leaderAwareness;
    }

    @Scheduled(fixedDelay = 1000)
    public void process() {
        try {
            leaderAwareness.executeIfLeader(outboxProcessor::process);
        } catch (Exception e) {
            logger.error("Problem while running outbox processor...", e);
        }
    }
}
