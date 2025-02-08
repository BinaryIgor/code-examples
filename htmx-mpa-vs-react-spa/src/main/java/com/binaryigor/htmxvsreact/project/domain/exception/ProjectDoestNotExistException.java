package com.binaryigor.htmxvsreact.project.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.DoesNotExistException;

import java.util.UUID;

public class ProjectDoestNotExistException extends DoesNotExistException {

    public ProjectDoestNotExistException(String message) {
        super(message);
    }

    public static ProjectDoestNotExistException ofId(UUID id) {
        return new ProjectDoestNotExistException("Project of %s id doesn't exist".formatted(id));
    }

    public static ProjectDoestNotExistException ofName(String name) {
        return new ProjectDoestNotExistException("Project of %s name doesn't exist".formatted(name));
    }
}
