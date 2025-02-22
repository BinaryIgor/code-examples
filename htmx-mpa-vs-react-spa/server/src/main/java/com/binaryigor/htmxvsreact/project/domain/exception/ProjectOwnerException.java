package com.binaryigor.htmxvsreact.project.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.OwnerException;

import java.util.UUID;

public class ProjectOwnerException extends OwnerException {

    public ProjectOwnerException(String message) {
        super(message);
    }

    public static ProjectOwnerException ofCurrentUser(UUID userId) {
        return new ProjectOwnerException("Project doesn't belong to current %s user".formatted(userId));
    }
}
