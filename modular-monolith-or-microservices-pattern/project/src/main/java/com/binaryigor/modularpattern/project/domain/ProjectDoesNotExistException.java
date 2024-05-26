package com.binaryigor.modularpattern.project.domain;

import java.util.UUID;

public class ProjectDoesNotExistException extends RuntimeException {

    public ProjectDoesNotExistException(String message) {
        super(message);
    }

    public static ProjectDoesNotExistException ofId(UUID id) {
        return new ProjectDoesNotExistException("Project of %s id does not exist".formatted(id));
    }
}
