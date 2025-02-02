package com.binaryigor.htmxvsreact.project.exception;

import com.binaryigor.htmxvsreact.shared.DoesNotExistException;

import java.util.UUID;

public class ProjectDoestNotExistException extends DoesNotExistException {

    public ProjectDoestNotExistException(String message) {
        super(message);
    }

    public static ProjectDoestNotExistException ofId(UUID id) {
        return new ProjectDoestNotExistException("Project of %s id doesn't exist".formatted(id));
    }
}
