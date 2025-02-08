package com.binaryigor.htmxvsreact.project.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.ValidationException;

public class ProjectNameException extends ValidationException {

    public final String name;

    public ProjectNameException(String name) {
        super("%s is not a valid project name");
        this.name = name;
    }
}
