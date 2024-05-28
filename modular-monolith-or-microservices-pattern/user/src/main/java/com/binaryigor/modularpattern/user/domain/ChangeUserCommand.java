package com.binaryigor.modularpattern.user.domain;

import java.util.UUID;

public record ChangeUserCommand(UUID id, String email, String name) {

    public User toUser(Long version) {
        return new User(id, email, name, version);
    }
}
