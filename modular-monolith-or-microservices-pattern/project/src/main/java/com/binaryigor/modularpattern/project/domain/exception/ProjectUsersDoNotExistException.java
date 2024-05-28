package com.binaryigor.modularpattern.project.domain.exception;

import java.util.Collection;
import java.util.UUID;

public class ProjectUsersDoNotExistException extends RuntimeException {

    public ProjectUsersDoNotExistException(String message) {
        super(message);
    }

    public static ProjectUsersDoNotExistException ofIds(Collection<UUID> ids) {
        return new ProjectUsersDoNotExistException("Project users of %s ids do not exist".formatted(ids));
    }
}
