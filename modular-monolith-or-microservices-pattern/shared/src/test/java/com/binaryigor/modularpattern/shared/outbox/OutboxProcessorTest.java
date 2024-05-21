package com.binaryigor.modularpattern.shared.outbox;

import com.binaryigor.modularpattern.shared.IntegrationTest;
import com.binaryigor.modularpattern.shared.events.AppEventsPublisher;
import com.binaryigor.modularpattern.shared.events.InMemoryAppEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OutboxProcessorTest extends IntegrationTest {

    private static final int MAX_EVENTS_TO_PROCESS = 3;
    private final JdbcClient jdbcClient = jdbcClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SqlOutboxRepository repository = new SqlOutboxRepository(jdbcClient, objectMapper);
    private final InMemoryAppEvents applicationEvents = new InMemoryAppEvents();
    private final AppEventsPublisher publisher = applicationEvents.publisher();
    private final OutboxProcessor processor = new OutboxProcessor(repository, publisher, MAX_EVENTS_TO_PROCESS);

    @BeforeEach
    void setup() {
        jdbcClient.sql("""
                CREATE TABLE IF NOT EXISTS outbox_message (
                  id UUID PRIMARY KEY,
                  message JSONB NOT NULL,
                  created_at TIMESTAMP NOT NULL DEFAULT NOW()
                );
                                
                TRUNCATE TABLE outbox_message;
                """).update();
    }

    @AfterEach
    void tearDown() {
        jdbcClient.sql("TRUNCATE TABLE outbox_message").update();
    }

    @Test
    void processesEventsByPublishingThemAndDeletingOnlySuccessfulOnes() {
        var event1 = new UserCreatedEvent(UUID.randomUUID(), "some name 1", "some-email-1@email.com");
        var event2 = new UserCreatedEvent(UUID.randomUUID(), "some name 2", "some-email-2@email.com");
        var event3 = new UserDeletedEvent(UUID.randomUUID());
        var event4 = new UserDeletedEvent(UUID.randomUUID());

        var outboxMessage1 = new OutboxMessage(event1);
        var outboxMessage2 = new OutboxMessage(event2);
        var outboxMessage3 = new OutboxMessage(event3);
        var outboxMessage4 = new OutboxMessage(event4);

        repository.save(List.of(outboxMessage1, outboxMessage2,
                outboxMessage3, outboxMessage4));

        var capturedUserCreatedEvents = new ArrayList<UserCreatedEvent>();
        var capturedUserDeletedEvents = new ArrayList<UserDeletedEvent>();

        applicationEvents.subscribe(UserCreatedEvent.class, capturedUserCreatedEvents::add);
        applicationEvents.subscribe(UserDeletedEvent.class, e -> {
            capturedUserDeletedEvents.add(e);
            throw new RuntimeException("Some Failure");
        });

        Assertions.assertThat(repository.all(Integer.MAX_VALUE))
                .hasSize(4);
        Assertions.assertThat(capturedUserCreatedEvents).isEmpty();
        Assertions.assertThat(capturedUserDeletedEvents).isEmpty();

        processor.process();

        Assertions.assertThat(repository.all(Integer.MAX_VALUE))
                .containsOnly(outboxMessage3, outboxMessage4);

        Assertions.assertThat(capturedUserCreatedEvents)
                .containsOnly(event1, event2);
        // event4 is outside all() limit as determined by MAX_EVENTS_TO_PROCESS
        Assertions.assertThat(capturedUserDeletedEvents)
                .containsOnly(event3);
    }

    public record UserCreatedEvent(UUID id, String name, String email) {
    }

    public record UserDeletedEvent(UUID id) {
    }
}
