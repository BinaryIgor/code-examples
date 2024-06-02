package com.binaryigor.modularpattern.user.domain.exception;

public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException(String message) {
        super(message);
    }
}
