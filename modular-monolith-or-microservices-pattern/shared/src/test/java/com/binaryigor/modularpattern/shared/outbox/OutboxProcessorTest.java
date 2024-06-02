package com.binaryigor.modularpattern.shared.outbox;

import com.binaryigor.modularpattern.shared.IntegrationTest;
import com.binaryigor.modularpattern.shared.TestClock;
import com.binaryigor.modularpattern.shared.events.AppEventsPublisher;
import com.binaryigor.modularpattern.shared.events.InMemoryAppEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OutboxProcessorTest extends IntegrationTest {

    private static final int MAX_EVENTS_TO_PROCESS = 4;
    private final TestClock clock = new TestClock();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SqlOutboxRepository repository = new SqlOutboxRepository(jdbcClient, objectMapper);
    private final InMemoryAppEvents applicationEvents = new InMemoryAppEvents();
    private final AppEventsPublisher publisher = applicationEvents.publisher();

    private final OutboxProcessor processor = new OutboxProcessor(repository, publisher, clock, MAX_EVENTS_TO_PROCESS);

    @BeforeEach
    void setup() {
        jdbcClient.sql("""
            CREATE TABLE IF NOT EXISTS outbox_message (
              id UUID PRIMARY KEY,
              message JSONB NOT NULL,
              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
              send_at TIMESTAMP NOT NULL DEFAULT NOW()
            );
                            
            TRUNCATE TABLE outbox_message;
            """).update();
    }

    @Test
    void processesScheduledEvents() {
        var event1 = new UserCreatedEvent(UUID.randomUUID(), "some name 1", "some-email-1@email.com");
        var event2 = new UserCreatedEvent(UUID.randomUUID(), "some name 2", "some-email-2@email.com");
        var event3 = new UserDeletedEvent(UUID.randomUUID());
        var event4 = new UserDeletedEvent(UUID.randomUUID());
        var event5 = new UserDeletedEvent(UUID.randomUUID());

        var outboxMessageSuccess1 = new OutboxMessage(event1);
        var outboxMessageSuccess2 = new OutboxMessage(event2);
        var outboxMessageFailure1 = new OutboxMessage(event3);
        var outboxMessageFailure2 = new OutboxMessage(event4);
        var outboxMessageFailure3 = new OutboxMessage(event5);
        var outboxMessageInTheFuture = new OutboxMessage(new UserDeletedEvent(UUID.randomUUID()));

        repository.save(List.of(outboxMessageSuccess1, outboxMessageSuccess2,
            outboxMessageFailure1, outboxMessageFailure2, outboxMessageFailure3,
            outboxMessageInTheFuture));

        var now = Instant.now();
        clock.setTime(now);

        updateMessageSendAt(outboxMessageInTheFuture.id(), now.plusSeconds(1));

        var capturedUserCreatedEvents = new ArrayList<UserCreatedEvent>();
        var capturedUserDeletedEvents = new ArrayList<UserDeletedEvent>();

        applicationEvents.subscribe(UserCreatedEvent.class, capturedUserCreatedEvents::add);
        applicationEvents.subscribe(UserDeletedEvent.class, e -> {
            capturedUserDeletedEvents.add(e);
            throw new RuntimeException("Some Failure");
        });

        Assertions.assertThat(repository.all()).hasSize(6);
        Assertions.assertThat(capturedUserCreatedEvents).isEmpty();
        Assertions.assertThat(capturedUserDeletedEvents).isEmpty();

        processor.process();

        Assertions.assertThat(repository.all())
            .containsOnly(outboxMessageFailure1, outboxMessageFailure2, outboxMessageFailure3,
                outboxMessageInTheFuture);

        Assertions.assertThat(capturedUserCreatedEvents)
            .containsOnly(event1, event2);
        // event5 is outside all() limit as determined by MAX_EVENTS_TO_PROCESS
        Assertions.assertThat(capturedUserDeletedEvents)
            .containsOnly(event3, event4);

        assertMessageSendAtIsInTheReasonableFuture(outboxMessageFailure1.id());
        assertMessageSendAtIsInTheReasonableFuture(outboxMessageFailure2.id());
    }

    private void updateMessageSendAt(UUID id, Instant sendAt) {
        jdbcClient.sql("UPDATE outbox_message SET send_at = ? WHERE id = ?")
            .params(Timestamp.from(sendAt), id)
            .update();
    }

    private void assertMessageSendAtIsInTheReasonableFuture(UUID id) {
        var sendAt = jdbcClient.sql("SELECT send_at FROM outbox_message WHERE id = ?")
            .params(id)
            .query(Timestamp.class)
            .single()
            .toInstant()
            .truncatedTo(ChronoUnit.SECONDS);

        var now = clock.instant().truncatedTo(ChronoUnit.SECONDS);

        Assertions.assertThat(sendAt)
            .isAfterOrEqualTo(now.plusSeconds(1))
            .isBeforeOrEqualTo(now.plusSeconds(15));
    }

    public record UserCreatedEvent(UUID id, String name, String email) {
    }

    public record UserDeletedEvent(UUID id) {
    }
}
