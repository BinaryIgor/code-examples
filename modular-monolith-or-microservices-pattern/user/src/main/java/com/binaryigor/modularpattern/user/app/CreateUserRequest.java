package com.binaryigor.modularpattern.user.app;

import com.binaryigor.modularpattern.user.domain.ChangeUserCommand;

import java.util.UUID;

public record CreateUserRequest(UUID id, String email, String name) {

    public ChangeUserCommand toChangeCommand() {
        return new ChangeUserCommand(id, email, name);
    }
}
