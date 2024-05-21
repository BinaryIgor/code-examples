package com.binaryigor.modularpattern.user.domain;

import com.binaryigor.modularpattern.shared.db.Transactions;
import com.binaryigor.modularpattern.shared.outbox.OutboxMessage;
import com.binaryigor.modularpattern.shared.outbox.OutboxRepository;

import java.util.UUID;

public class UserService {

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

    public void create(User user) {
        //TODO: validate & send event
        transactions.execute(() -> {
            userRepository.save(user);
            outboxRepository.save(new OutboxMessage(user.toUserChangedEvent()));
        });
    }

    public void update(User user) {
        //TODO: validate & send event
        transactions.execute(() -> {
            userRepository.save(user);
            outboxRepository.save(new OutboxMessage(user.toUserChangedEvent()));
        });
    }

    public User ofId(UUID id) {
        return userRepository.ofId(id).orElseThrow(() -> UserDoesNotExistException.ofId(id));
    }
}
