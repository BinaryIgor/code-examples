package com.binaryigor.modularpattern.user;

import com.binaryigor.modularpattern.shared.contracts.UserChangedEvent;
import com.binaryigor.modularpattern.user.domain.User;

import java.util.Random;
import java.util.UUID;

public class TestObjects {

    private static final Random RANDOM = new Random();

    public static User randomUser(Long version) {
        var id = UUID.randomUUID();
        return new User(id, id + "@email.com", id + "-name", version);
    }

    public static User randomNewUser() {
        return randomUser(null);
    }

    public static UserChangedEvent randomUserChangedEvent() {
        return randomUser(1 + RANDOM.nextLong(100)).toUserChangedEvent();
    }

    public static User userWithIncreasedVersion(User user) {
        return new User(user.id(), user.email(), user.name(), user.version() == null ? 1 : user.version() + 1);
    }
}
