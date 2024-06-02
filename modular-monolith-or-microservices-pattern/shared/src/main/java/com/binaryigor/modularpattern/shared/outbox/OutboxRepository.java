package com.binaryigor.modularpattern.shared.outbox;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OutboxRepository {

    void save(OutboxMessage message);

    void save(Collection<OutboxMessage> messages);

    List<OutboxMessage> toSend(Instant now, int limit);

    List<OutboxMessage> all();

    void delete(Collection<UUID> ids);

    void updateSendAt(Map<UUID, Instant> idsSendAt);
}
