package com.binaryigor.modularpattern.user.app;

import com.binaryigor.modularpattern.user.domain.ChangeUserCommand;

import java.util.UUID;

public record UpdateUserRequest(String email, String name) {

    public ChangeUserCommand toChangeCommand(UUID id) {
        return new ChangeUserCommand(id, email, name);
    }
}
