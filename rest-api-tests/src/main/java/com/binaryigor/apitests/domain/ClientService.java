package com.binaryigor.apitests.domain;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ClientService {

    private final ClientRepository repository;

    public ClientService(ClientRepository repository) {
        this.repository = repository;
    }

    public void create(Client client) {
        repository.create(client);
    }

    public void update(Client client) {
        var currentClient = repository.getById(client.id());

        if (currentClient.isEmpty()) {
            throw new ClientNotFoundException(client.id());
        }

        repository.update(client);
    }

    public Client get(UUID id) {
        return repository.getById(id).orElseThrow(() -> new ClientNotFoundException(id));
    }
}
