package com.binaryigor.modularmonolith.backgroundsync.shared.outbox;

import java.util.UUID;

public record OutboxMessage(UUID id, Object data) {

    public OutboxMessage(Object data) {
        this(UUID.randomUUID(), data);
    }
}
