package com.binaryigor.htmxvsreact.task.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.ValidationException;

public class TaskStatusValidationException extends ValidationException {

    public final String status;

    public TaskStatusValidationException(String status) {
        super("%s is not a valid status".formatted(status));
        this.status = status;
    }
}
