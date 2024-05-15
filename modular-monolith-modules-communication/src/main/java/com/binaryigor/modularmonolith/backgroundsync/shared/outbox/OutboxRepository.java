package com.binaryigor.modularmonolith.backgroundsync.shared.outbox;

import com.binaryigor.modularmonolith.backgroundsync.shared.outbox.OutboxMessage;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository {

    void save(OutboxMessage message);

    List<OutboxMessage> get(int limit);

    void delete(Collection<UUID> ids);
}
