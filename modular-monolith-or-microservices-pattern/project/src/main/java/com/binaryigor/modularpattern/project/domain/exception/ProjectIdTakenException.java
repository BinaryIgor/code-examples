package com.binaryigor.modularpattern.project.domain.exception;

public class ProjectIdTakenException extends RuntimeException {

    public ProjectIdTakenException() {
        super("Project of given id exists already");
    }

}
