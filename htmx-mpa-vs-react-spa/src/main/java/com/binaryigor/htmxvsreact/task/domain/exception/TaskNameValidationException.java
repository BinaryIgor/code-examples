package com.binaryigor.htmxvsreact.task.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.ValidationException;

public class TaskNameValidationException extends ValidationException {

    public final String name;

    public TaskNameValidationException(String name) {
        super("%s is not a valid task name");
        this.name = name;
    }
}
