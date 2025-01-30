package com.binaryigor.htmxvsreact.project;

import java.util.UUID;

public record Project(UUID id, String name, UUID ownerId) {

    public Project {
        if (name == null || name.isBlank()) {
            throw new ProjectValidationException("Name cannot be empty!");
        }
        if (name.length() < 3 || name.length() > 100) {
            throw new ProjectValidationException("Name length must be between 3 and 100 characters!");
        }
    }

    public static Project newOne(String name, UUID ownerId) {
        return new Project(UUID.randomUUID(), name, ownerId);
    }
}
