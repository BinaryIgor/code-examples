package com.binaryigor.modularpattern.user.domain.exception;

public class UserEmailTakenException extends RuntimeException {

    public UserEmailTakenException() {
        super("User of given email exists already");
    }

}
