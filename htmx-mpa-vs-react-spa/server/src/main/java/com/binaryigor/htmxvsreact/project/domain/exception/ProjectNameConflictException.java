package com.binaryigor.htmxvsreact.project.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.ConflictException;

public class ProjectNameConflictException extends ConflictException {

    public final String name;

    public ProjectNameConflictException(String name) {
        super("Project of %s name exists already".formatted(name));
        this.name = name;
    }
}
