package com.binaryigor.modularpattern.shared.outbox;

import java.util.UUID;

public record OutboxMessage(UUID id, Object message) {

    public OutboxMessage(Object message) {
        this(UUID.randomUUID(), message);
    }
}
