package com.binaryigor.modularmonolith.backgroundsync.shared.outbox;

import com.binaryigor.modularmonolith.backgroundsync.shared.events.ApplicationEventPublisher;

import java.util.ArrayList;
import java.util.UUID;

public class OutboxProcessor {

    private final ApplicationEventPublisher publisher;
    private final OutboxRepository outboxRepository;
    private final int processBatch;

    public OutboxProcessor(ApplicationEventPublisher publisher,
                           OutboxRepository outboxRepository,
                           int processBatch) {
        this.publisher = publisher;
        this.outboxRepository = outboxRepository;
        this.processBatch = processBatch;
    }

    public void run() {
        var messagesToPublish = outboxRepository.get(processBatch);
        if (messagesToPublish.isEmpty()) {
            return;
        }

        var publishedMessages = new ArrayList<UUID>();
        var failedMessages = new ArrayList<UUID>();

        messagesToPublish.forEach(m -> {
            try {
                publisher.publish(m.data());
                publishedMessages.add(m.id());
            } catch (Exception e) {
                System.err.println("Failure while publishing outbox message...");
                e.printStackTrace();
                failedMessages.add(m.id());
            }
        });

        System.out.printf("Outbox: successfully published %d messages, failed with %d messages%n", publishedMessages.size(), failedMessages.size());

        outboxRepository.delete(publishedMessages);
    }
}
