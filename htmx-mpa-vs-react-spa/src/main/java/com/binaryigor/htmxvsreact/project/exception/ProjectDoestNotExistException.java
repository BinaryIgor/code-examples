package com.binaryigor.htmxvsreact.project.exception;

public class ProjectDoestNotExistException extends RuntimeException {

    public ProjectDoestNotExistException(String message) {
        super(message);
    }
}
