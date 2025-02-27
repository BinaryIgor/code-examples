package com.binaryigor.htmxvsreact.task.domain.exception;

import com.binaryigor.htmxvsreact.shared.exception.ValidationException;

public class TaskProjectRequiredException extends ValidationException {

    public TaskProjectRequiredException() {
        super("Task project is required");
    }
}
