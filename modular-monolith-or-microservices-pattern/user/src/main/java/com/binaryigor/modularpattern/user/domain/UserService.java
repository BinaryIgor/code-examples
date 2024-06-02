package com.binaryigor.modularpattern.user.domain;

import com.binaryigor.modularpattern.shared.contracts.UserClient;
import com.binaryigor.modularpattern.shared.contracts.UserView;
import com.binaryigor.modularpattern.shared.db.Transactions;
import com.binaryigor.modularpattern.shared.outbox.OutboxMessage;
import com.binaryigor.modularpattern.shared.outbox.OutboxRepository;
import com.binaryigor.modularpattern.user.domain.exception.UserDoesNotExistException;
import com.binaryigor.modularpattern.user.domain.exception.UserEmailTakenException;
import com.binaryigor.modularpattern.user.domain.exception.UserIdTakenException;

import java.util.UUID;
import java.util.stream.Stream;

//TODO: basic name/email validation
public class UserService implements UserClient {

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

    public User create(ChangeUserCommand command) {
        if (userRepository.ofId(command.id()).isPresent()) {
            throw new UserIdTakenException();
        }
        if (userRepository.ofEmail(command.email()).isPresent()) {
            throw new UserEmailTakenException();
        }

        return transactions.executeAndReturn(() -> {
            var createdUser = userRepository.save(command.toUser(null));
            outboxRepository.save(new OutboxMessage(createdUser.toUserChangedEvent()));
            return createdUser;
        });
    }

    public User update(ChangeUserCommand command) {
        var user = ofId(command.id());

        if (!user.email().equals(command.email())
            && userRepository.ofEmail(command.email()).isPresent()) {
            throw new UserEmailTakenException();
        }

        return transactions.executeAndReturn(() -> {
            var toUpdateUser = command.toUser(user.version());
            var updatedUser = userRepository.save(toUpdateUser);
            outboxRepository.save(new OutboxMessage(updatedUser.toUserChangedEvent()));
            return updatedUser;
        });
    }

    public User ofId(UUID id) {
        return userRepository.ofId(id).orElseThrow(() -> UserDoesNotExistException.ofId(id));
    }

    @Override
    public Stream<UserView> allUsers() {
        return userRepository.allUsers().map(User::toView);
    }
}
