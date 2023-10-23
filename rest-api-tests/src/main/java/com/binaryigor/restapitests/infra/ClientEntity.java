package com.binaryigor.restapitests.infra;

import com.binaryigor.restapitests.domain.Client;
import com.binaryigor.restapitests.domain.ClientStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("client")
public record ClientEntity(
        @Id
        UUID id,
        String name,
        String email,
        String status
) {

    public static ClientEntity fromClient(Client client) {
        return new ClientEntity(client.id(), client.name(), client.email(), client.status().name());
    }

    public Client toClient() {
        return new Client(id, name, email, ClientStatus.valueOf(status));
    }

}
