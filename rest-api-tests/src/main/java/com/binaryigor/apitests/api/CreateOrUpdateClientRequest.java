package com.binaryigor.apitests.api;

import com.binaryigor.apitests.domain.Client;
import com.binaryigor.apitests.domain.ClientStatus;

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
