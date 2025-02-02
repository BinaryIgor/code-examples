package com.binaryigor.htmxvsreact.project.exception;

import com.binaryigor.htmxvsreact.shared.ValidationException;

public class ProjectNameConflictException extends ValidationException {

    public final String name;

    public ProjectNameConflictException(String name) {
        super("Project of %s name exists already".formatted(name));
        this.name = name;
    }
}
