package com.binaryigor.modularmonolith.backgroundsync;

import com.binaryigor.modularmonolith.backgroundsync.project.InMemoryProjectUserRepository;
import com.binaryigor.modularmonolith.backgroundsync.project.ProjectUsersSynchronization;
import com.binaryigor.modularmonolith.backgroundsync.shared.Transactions;
import com.binaryigor.modularmonolith.backgroundsync.shared.events.InMemoryApplicationEvents;
import com.binaryigor.modularmonolith.backgroundsync.shared.outbox.InMemoryOutboxRepository;
import com.binaryigor.modularmonolith.backgroundsync.shared.outbox.OutboxProcessor;
import com.binaryigor.modularmonolith.backgroundsync.user.CreateUserCommand;
import com.binaryigor.modularmonolith.backgroundsync.user.InMemoryUserRepository;
import com.binaryigor.modularmonolith.backgroundsync.user.UserService;

import java.util.UUID;

public class Main {
    public static void main(String[] args) {
        var projectUserRepository = new InMemoryProjectUserRepository();

        var userRepository = new InMemoryUserRepository();

        var outboxRepository = new InMemoryOutboxRepository();

        var events = new InMemoryApplicationEvents();

        var userService = new UserService(userRepository, outboxRepository, Transactions.DELEGATE);

        var projectUsersSynchronization = new ProjectUsersSynchronization(projectUserRepository, userService, events);

        var outboxProcessor = new OutboxProcessor(events, outboxRepository, 100);

        userService.create(new CreateUserCommand("user1", "user1@gmail.com"));

        outboxProcessor.run();

        projectUsersSynchronization.synchronizeAll();
    }

    record TestEvent(UUID id, String name) {

    }
}