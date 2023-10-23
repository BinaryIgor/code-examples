package com.binaryigor.restapitests.api;

import com.binaryigor.restapitests.domain.Client;
import com.binaryigor.restapitests.domain.ClientStatus;

import java.util.UUID;

public record CreateOrUpdateClientRequest(String name,
                                          String email,
                                          ClientStatus status) {

    public Client toClient() {
        return toClient(UUID.randomUUID());
    }

    public Client toClient(UUID id) {
        return new Client(id, name, email, status);
    }
}
