package com.binaryigor.restapitests.api;

import com.binaryigor.restapitests.domain.Client;
import com.binaryigor.restapitests.domain.ClientService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private final ClientService service;

    public ClientController(ClientService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    CreateClientResponse create(@RequestBody CreateOrUpdateClientRequest request) {
        var newClient = request.toClient();

        service.create(newClient);

        return new CreateClientResponse(newClient.id());
    }

    @PutMapping("{id}")
    void update(@PathVariable UUID id,
                @RequestBody CreateOrUpdateClientRequest request) {
        service.update(request.toClient(id));
    }

    @GetMapping("/{id}")
    Client get(@PathVariable UUID id) {
        return service.get(id);
    }
}
