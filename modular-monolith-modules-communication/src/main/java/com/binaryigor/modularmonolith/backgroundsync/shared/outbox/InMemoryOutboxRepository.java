package com.binaryigor.modularmonolith.backgroundsync.shared.outbox;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryOutboxRepository implements OutboxRepository {

    private final Map<UUID, OutboxMessage> db = new ConcurrentHashMap<>();

    @Override
    public void save(OutboxMessage message) {
        db.put(message.id(), message);
    }

    @Override
    public List<OutboxMessage> get(int limit) {
        return db.values().stream().limit(limit).toList();
    }

    @Override
    public void delete(Collection<UUID> ids) {
        ids.forEach(db::remove);
    }
}
