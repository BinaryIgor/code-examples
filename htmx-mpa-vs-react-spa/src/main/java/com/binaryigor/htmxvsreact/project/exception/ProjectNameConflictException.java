package com.binaryigor.htmxvsreact.project.exception;

public class ProjectNameConflictException extends RuntimeException {

    public final String name;

    public ProjectNameConflictException(String name) {
        this.name = name;
    }
}
