package com.binaryigor.htmxvsreact.task.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.OwnerException;

import java.util.UUID;

public class TaskProjectOwnerException extends OwnerException {

    public TaskProjectOwnerException(String message) {
        super(message);
    }

    public static TaskProjectOwnerException ofCurrentUser(UUID userId) {
        return new TaskProjectOwnerException("Task project doesn't belong to current %s user".formatted(userId));
    }
}
