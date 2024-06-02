package com.binaryigor.modularpattern.shared.outbox;

import com.binaryigor.modularpattern.shared.events.AppEventsPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OutboxProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OutboxProcessor.class);
    private final OutboxRepository outboxRepository;
    private final AppEventsPublisher appEventsPublisher;
    private final Function<Instant, Instant> nextFailedEventSendAt;
    private final Clock clock;
    private final int maxEventsToProcess;

    public OutboxProcessor(OutboxRepository outboxRepository,
                           AppEventsPublisher appEventsPublisher,
                           Function<Instant, Instant> nextFailedEventSendAt,
                           Clock clock,
                           int maxEventsToProcess) {
        this.outboxRepository = outboxRepository;
        this.appEventsPublisher = appEventsPublisher;
        this.nextFailedEventSendAt = nextFailedEventSendAt;
        this.clock = clock;
        this.maxEventsToProcess = maxEventsToProcess;
    }

    public OutboxProcessor(OutboxRepository outboxRepository,
                           AppEventsPublisher appEventsPublisher,
                           Clock clock,
                           int maxEventsToProcess) {
        this(outboxRepository, appEventsPublisher, new Function<>() {

            private final Random random = new Random();

            @Override
            public Instant apply(Instant instant) {
                return instant.plusSeconds(1 + random.nextInt(15));
            }
        }, clock, maxEventsToProcess);
    }

    public void process() {
        var eventsToSend = outboxRepository.toSend(clock.instant(), maxEventsToProcess);
        var sentEventIds = new ArrayList<UUID>();
        var failedEventIds = new ArrayList<UUID>();

        for (var e : eventsToSend) {
            try {
                appEventsPublisher.publish(e.message());
                sentEventIds.add(e.id());
            } catch (Exception ex) {
                failedEventIds.add(e.id());
            }
        }

        outboxRepository.delete(sentEventIds);
        if (!failedEventIds.isEmpty()) {
            logger.warn("Failed to send some events. Will retry according to the events schedule: {}", failedEventIds);
            updateFailedEventsSendAt(failedEventIds);
        }
    }

    private void updateFailedEventsSendAt(List<UUID> eventIds) {
        var now = clock.instant();
        var idsSendAt = eventIds.stream()
            .collect(Collectors.toMap(Function.identity(), id -> nextFailedEventSendAt.apply(now)));
        outboxRepository.updateSendAt(idsSendAt);
    }
}
