package com.binaryigor.htmxvsreact.project.exception;

import java.util.UUID;

public class ProjectOwnerException extends RuntimeException {

    public ProjectOwnerException(String message) {
        super(message);
    }

    public static ProjectOwnerException ofCurrentUser(UUID userId) {
        return new ProjectOwnerException("Project doesn't belong to current %s user".formatted(userId));
    }
}
