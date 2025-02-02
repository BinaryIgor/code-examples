package com.binaryigor.htmxvsreact.task.exception;

import com.binaryigor.htmxvsreact.shared.DoesNotExistException;

public class TaskDoestNotExistException extends DoesNotExistException {

    public TaskDoestNotExistException(String message) {
        super(message);
    }
}
