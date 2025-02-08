package com.binaryigor.htmxvsreact.project.domain;

import com.binaryigor.htmxvsreact.project.domain.exception.ProjectNameException;
import com.binaryigor.htmxvsreact.shared.FieldValidator;
import com.binaryigor.htmxvsreact.shared.contracts.ProjectView;

import java.util.UUID;

public record Project(UUID id, String name, UUID ownerId) {

    public Project {
        if (!FieldValidator.isNameValid(name, 3, 50)) {
            throw new ProjectNameException(name);
        }
    }

    public static Project newOne(String name, UUID ownerId) {
        return new Project(UUID.randomUUID(), name, ownerId);
    }

    public ProjectView toView() {
        return new ProjectView(id, name, ownerId);
    }
}
