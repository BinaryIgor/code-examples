package com.binaryigor.restapitests.domain;

import java.util.Optional;
import java.util.UUID;

public interface ClientRepository {

    void create(Client client);

    void update(Client client);

    Optional<Client> getById(UUID id);
}
