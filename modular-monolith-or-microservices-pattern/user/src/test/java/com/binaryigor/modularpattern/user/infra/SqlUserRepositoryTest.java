package com.binaryigor.modularpattern.user.infra;

import com.binaryigor.modularpattern.user.IntegrationTest;
import com.binaryigor.modularpattern.user.domain.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.stream.Stream;

public class SqlUserRepositoryTest extends IntegrationTest {

    @Autowired
    private SqlUserRepository repository;

    @Test
    void returnsStreamOfAllUsers() {
        var allUsers = Stream.generate(this::randomUser)
            .limit(25)
            .toList();

        allUsers.forEach(u -> repository.save(u));

        var expectedStreamOfUsers = allUsers.stream()
            .map(User::toView)
            .toList();

        try (var actualStreamOfUsers = repository.allUsers()) {
            Assertions.assertThat(actualStreamOfUsers.toList())
                .containsExactlyElementsOf(expectedStreamOfUsers);
        }
    }

    private User randomUser() {
        var id = UUID.randomUUID();
        return new User(id, id + "@email.com", id + "-name");
    }
}
