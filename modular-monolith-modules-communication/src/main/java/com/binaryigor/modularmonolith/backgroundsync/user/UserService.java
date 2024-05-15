package com.binaryigor.modularmonolith.backgroundsync.user;

import com.binaryigor.modularmonolith.backgroundsync.shared.outbox.OutboxMessage;
import com.binaryigor.modularmonolith.backgroundsync.shared.outbox.OutboxRepository;
import com.binaryigor.modularmonolith.backgroundsync.shared.Transactions;
import com.binaryigor.modularmonolith.backgroundsync.shared.api.UserApi;
import com.binaryigor.modularmonolith.backgroundsync.shared.api.UserView;

import java.util.UUID;
import java.util.stream.Stream;

public class UserService implements UserApi {

    private final UserRepository userRepository;
    private final OutboxRepository outboxRepository;
    private final Transactions transactions;

    public UserService(UserRepository userRepository,
                       OutboxRepository outboxRepository,
                       Transactions transactions) {
        this.userRepository = userRepository;
        this.outboxRepository = outboxRepository;
        this.transactions = transactions;
    }

    public UUID create(CreateUserCommand command) {
        // TODO some validation
        var user = command.toUser();
        transactions.execute(() -> {
            userRepository.save(user);
            outboxRepository.save(new OutboxMessage(user.toChangedEvent()));
        });
        return user.id();
    }

    public void update(UpdateUserCommand command) {
        // TODO some validation
        if (userRepository.ofId(command.id()).isEmpty()) {
            throw new RuntimeException("User does not exist");
        }
        transactions.execute(() -> {
            var user = new User(command.id(), command.name(), command.email());
            userRepository.save(user);
            outboxRepository.save(new OutboxMessage(user.toChangedEvent()));
        });
    }

    @Override
    public Stream<UserView> allUsers() {
        return userRepository.allUsers().map(u -> new UserView(u.id(), u.name(), u.email()));
    }
}
