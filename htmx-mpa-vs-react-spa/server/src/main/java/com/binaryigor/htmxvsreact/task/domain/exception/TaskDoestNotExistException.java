package com.binaryigor.htmxvsreact.task.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.DoesNotExistException;

import java.util.UUID;

public class TaskDoestNotExistException extends DoesNotExistException {

    public TaskDoestNotExistException(String message) {
        super(message);
    }

    public static TaskDoestNotExistException ofId(UUID id) {
        return new TaskDoestNotExistException("Task of %s id doesn't exist".formatted(id));
    }
}
