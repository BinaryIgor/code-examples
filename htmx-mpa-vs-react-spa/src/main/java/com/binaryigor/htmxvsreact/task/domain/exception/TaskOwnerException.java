package com.binaryigor.htmxvsreact.task.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.OwnerException;

import java.util.UUID;

public class TaskOwnerException extends OwnerException {

    public TaskOwnerException(String message) {
        super(message);
    }

    public static TaskOwnerException ofCurrentUser(UUID userId) {
        return new TaskOwnerException("Task doesn't belong to current %s user".formatted(userId));
    }
}
