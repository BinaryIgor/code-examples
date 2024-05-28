package com.binaryigor.modularpattern.user.domain.exception;

public class UserIdTakenException extends RuntimeException {

    public UserIdTakenException() {
        super("User of given id exists already");
    }

}
