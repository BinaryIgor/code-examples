package com.binaryigor.htmxvsreact.task.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.ValidationException;

import java.util.Collection;

public class TaskProjectValidationException extends ValidationException {

    public final Collection<String> allowedProjects;

    public TaskProjectValidationException(Collection<String> allowedProjects) {
        super("Invalid task project. Allowed are: %s".formatted(String.join(", ", allowedProjects)));
        this.allowedProjects = allowedProjects;
    }
}
