package com.binaryigor.modularmonolith.backgroundsync.user;

import java.util.UUID;

public record CreateUserCommand(String name, String email) {

    public User toUser() {
        return new User(UUID.randomUUID(), name, email);
    }
}
