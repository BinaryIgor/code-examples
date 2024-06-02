package com.binaryigor.modularpattern.user.domain;

import com.binaryigor.modularpattern.shared.contracts.UserChangedEvent;
import com.binaryigor.modularpattern.shared.contracts.UserView;

import java.util.UUID;

public record User(UUID id, String email, String name, Long version) {

    public UserView toView() {
        return new UserView(id, email, name, version == null ? 1 : version);
    }

    public UserChangedEvent toUserChangedEvent() {
        return new UserChangedEvent(toView());
    }
}
