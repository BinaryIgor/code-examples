package com.binaryigor.modularmonolith.backgroundsync.user;

import com.binaryigor.modularmonolith.backgroundsync.shared.api.UserChangedEvent;

import java.util.UUID;

public record User(UUID id, String name, String email) {

    public UserChangedEvent toChangedEvent() {
        return new UserChangedEvent(id, name, email);
    }
}
