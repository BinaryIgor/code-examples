package com.binaryigor.modularmonolith.backgroundsync.user;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class InMemoryUserRepository implements UserRepository {

    private final Map<UUID, User> db = new ConcurrentHashMap<>();

    @Override
    public void save(User user) {
        db.put(user.id(), user);
    }

    @Override
    public Optional<User> ofId(UUID id) {
        return Optional.ofNullable(db.get(id));
    }

    @Override
    public Stream<User> allUsers() {
        return db.values().stream();
    }
}
