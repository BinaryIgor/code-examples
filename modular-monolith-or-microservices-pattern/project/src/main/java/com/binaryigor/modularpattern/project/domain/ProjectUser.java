package com.binaryigor.modularpattern.project.domain;

import com.binaryigor.modularpattern.shared.contracts.UserView;

import java.util.UUID;

public record ProjectUser(UUID id, String email, String name) {

    public static ProjectUser fromUserView(UserView view) {
        return new ProjectUser(view.id(), view.email(), view.name());
    }
}
