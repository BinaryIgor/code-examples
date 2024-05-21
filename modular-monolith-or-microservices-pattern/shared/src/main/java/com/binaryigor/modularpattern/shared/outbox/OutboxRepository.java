package com.binaryigor.modularpattern.shared.outbox;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface OutboxRepository {

    void save(OutboxMessage message);

    void save(Collection<OutboxMessage> messages);

    List<OutboxMessage> all(int limit);

    void delete(Collection<UUID> ids);
}
