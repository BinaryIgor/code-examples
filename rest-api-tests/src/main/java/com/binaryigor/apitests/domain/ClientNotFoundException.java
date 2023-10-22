package com.binaryigor.apitests.domain;

import java.util.UUID;

public class ClientNotFoundException extends RuntimeException {

    public ClientNotFoundException(UUID id) {
        super("Client of %s id doesn't exist".formatted(id));
    }
}
