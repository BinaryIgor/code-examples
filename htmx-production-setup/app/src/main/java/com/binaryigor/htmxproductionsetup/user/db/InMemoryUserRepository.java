package com.binaryigor.htmxproductionsetup.user.db;

import com.binaryigor.htmxproductionsetup.user.domain.User;
import com.binaryigor.htmxproductionsetup.user.domain.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryUserRepository implements UserRepository {

    private final Map<UUID, User> db = new ConcurrentHashMap<>();

    public InMemoryUserRepository() {
        var user1 = new User(UUID.fromString("bdcdc153-3865-42b2-9f7d-4c380aa13c80"), "igor.thebinary@gmail.com", "Igor", "ComplexPassword12");
        db.put(user1.id(), user1);
    }

    @Override
    public Optional<User> ofEmail(String email) {
        return db.values().stream().filter(u -> u.email().equals(email)).findAny();
    }

    @Override
    public Optional<User> ofId(UUID id) {
        return Optional.ofNullable(db.get(id));
    }
}
