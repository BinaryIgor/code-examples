package com.binaryigor.htmxvsreact.project.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.ValidationException;

public class ProjectNameValidationException extends ValidationException {

    public final String name;

    public ProjectNameValidationException(String name) {
        super("%s is not a valid project name");
        this.name = name;
    }
}
