package com.binaryigor.htmxvsreact.task.domain;

import com.binaryigor.htmxvsreact.task.domain.exception.TaskStatusValidationException;

public enum TaskStatus {
    TODO, IN_PROGRESS, CANCELED, DONE;

    public static TaskStatus fromString(String status) {
        try {
            return TaskStatus.valueOf(status.trim());
        } catch (Exception e) {
            throw new TaskStatusValidationException(status);
        }
    }
}
