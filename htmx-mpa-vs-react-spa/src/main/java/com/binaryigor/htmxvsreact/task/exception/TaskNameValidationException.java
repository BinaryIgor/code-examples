package com.binaryigor.htmxvsreact.task.exception;

import com.binaryigor.htmxvsreact.shared.ValidationException;

public class TaskNameValidationException extends ValidationException {

    public TaskNameValidationException(String message) {
        super(message);
    }
}
