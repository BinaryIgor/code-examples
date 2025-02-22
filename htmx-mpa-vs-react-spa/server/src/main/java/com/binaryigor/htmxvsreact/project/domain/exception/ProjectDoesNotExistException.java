package com.binaryigor.htmxvsreact.project.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.DoesNotExistException;

import java.util.UUID;

public class ProjectDoesNotExistException extends DoesNotExistException {

    public ProjectDoesNotExistException(String message) {
        super(message);
    }

    public static ProjectDoesNotExistException ofId(UUID id) {
        return new ProjectDoesNotExistException("Project of %s id doesn't exist".formatted(id));
    }

    public static ProjectDoesNotExistException ofName(String name) {
        return new ProjectDoesNotExistException("Project of %s name doesn't exist".formatted(name));
    }
}
