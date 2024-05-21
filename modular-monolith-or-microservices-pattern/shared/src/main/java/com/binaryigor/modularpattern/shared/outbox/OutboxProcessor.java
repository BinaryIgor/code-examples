package com.binaryigor.modularpattern.shared.outbox;

import com.binaryigor.modularpattern.shared.events.AppEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.UUID;

public class OutboxProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OutboxProcessor.class);
    private final OutboxRepository outboxRepository;
    private final AppEventsPublisher appEventsPublisher;
    private final int maxEventsToProcess;

    public OutboxProcessor(OutboxRepository outboxRepository,
                           AppEventsPublisher appEventsPublisher,
                           int maxEventsToProcess) {
        this.outboxRepository = outboxRepository;
        this.appEventsPublisher = appEventsPublisher;
        this.maxEventsToProcess = maxEventsToProcess;
    }

    public void process() {
        var eventsToSend = outboxRepository.all(maxEventsToProcess);
        var sendEventIds = new ArrayList<UUID>();
        var failedEventIds = new ArrayList<UUID>();

        // TODO: maybe handle in multiple threads
        for (var e : eventsToSend) {
            try {
                appEventsPublisher.publish(e.message());
                sendEventIds.add(e.id());
            } catch (Exception ex) {
                failedEventIds.add(e.id());
            }
        }

        outboxRepository.delete(sendEventIds);
        if (!failedEventIds.isEmpty()) {
            logger.warn("Failed to send some events. Will retry in the next run: {}", failedEventIds);
        }
    }
}
