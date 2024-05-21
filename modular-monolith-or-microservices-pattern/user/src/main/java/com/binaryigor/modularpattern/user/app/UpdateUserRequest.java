package com.binaryigor.modularpattern.user.app;

import com.binaryigor.modularpattern.user.domain.User;

import java.util.UUID;

public record UpdateUserRequest(String email, String name) {

    public User toUser(UUID id) {
        return new User(id, email, name);
    }
}
