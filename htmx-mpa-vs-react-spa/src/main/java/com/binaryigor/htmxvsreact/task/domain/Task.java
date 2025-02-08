package com.binaryigor.htmxvsreact.task.domain;

import com.binaryigor.htmxvsreact.shared.FieldValidator;
import com.binaryigor.htmxvsreact.task.domain.exception.TaskNameValidationException;

import java.util.UUID;

public record Task(UUID id, String name, UUID projectId, UUID ownerId, TaskStatus status) {

    public Task {
        if (!FieldValidator.isNameValid(name, 3, 50)) {
            throw new TaskNameValidationException(name);
        }
    }
}
