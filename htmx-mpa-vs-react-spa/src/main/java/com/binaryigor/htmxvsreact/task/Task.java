package com.binaryigor.htmxvsreact.task;

import com.binaryigor.htmxvsreact.task.exception.TaskNameValidationException;

import java.util.UUID;

public record Task(UUID id, String name, UUID projectId, TaskStatus status) {

    public Task {
        if (name == null || name.isBlank()) {
            throw new TaskNameValidationException("Name cannot be empty!");
        }
        if (name.length() < 3 || name.length() > 100) {
            throw new TaskNameValidationException("Name length must be between 3 and 100 characters!");
        }
    }
}
