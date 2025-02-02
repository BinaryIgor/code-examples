package com.binaryigor.htmxvsreact.project.exception;

import com.binaryigor.htmxvsreact.shared.ValidationException;

import java.util.UUID;

public class ProjectOwnerException extends ValidationException {

    public ProjectOwnerException(String message) {
        super(message);
    }

    public static ProjectOwnerException ofCurrentUser(UUID userId) {
        return new ProjectOwnerException("Project doesn't belong to current %s user".formatted(userId));
    }
}
