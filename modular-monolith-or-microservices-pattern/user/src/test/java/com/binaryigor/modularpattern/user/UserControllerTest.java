package com.binaryigor.modularpattern.user;

import com.binaryigor.modularpattern.shared.contracts.UserChangedEvent;
import com.binaryigor.modularpattern.shared.outbox.OutboxMessage;
import com.binaryigor.modularpattern.user.app.UpdateUserRequest;
import com.binaryigor.modularpattern.user.domain.User;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public class UserControllerTest extends IntegrationTest {

    @Test
    void createsUpdatesAndGetsUser() {
        var newUser = new User(UUID.randomUUID(), "some-email@email.com", "some-name");

        var createUserResponse = createUser(newUser);

        Assertions.assertThat(createUserResponse.getStatusCode())
                .isEqualTo(HttpStatus.CREATED);

        var userId = newUser.id();

        var updateUserRequest = new UpdateUserRequest("some-email2@email.com", "some-name-2");

        var updateUserResponse = updateUser(userId, updateUserRequest);

        var expectedUserAfterUpdate = updateUserRequest.toUser(userId);

        Assertions.assertThat(updateUserResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        Assertions.assertThat(updateUserResponse.getBody())
                .isEqualTo(expectedUserAfterUpdate);

        var getUserResponse = getUser(userId);
        Assertions.assertThat(getUserResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        Assertions.assertThat(getUserResponse.getBody())
                .isEqualTo(expectedUserAfterUpdate);

        var userCreatedEvent = createUserResponse.getBody().toUserChangedEvent();
        var userUpdatedEvent = updateUserResponse.getBody().toUserChangedEvent();

        assertUserChangedEventsWerePublishedToOutbox(userCreatedEvent, userUpdatedEvent);
    }

    private ResponseEntity<User> createUser(User user) {
        return restTemplate.postForEntity("/users", user, User.class);
    }

    private ResponseEntity<User> updateUser(UUID id, UpdateUserRequest request) {
        return restTemplate.exchange(RequestEntity.put("/users/" + id).body(request), User.class);
    }

    private ResponseEntity<User> getUser(UUID id) {
        return restTemplate.getForEntity("/users/" + id, User.class);
    }

    private void assertUserChangedEventsWerePublishedToOutbox(UserChangedEvent... events) {
        var messages = userOutboxRepository.all(Integer.MAX_VALUE).stream().map(OutboxMessage::message).toList();
        Assertions.assertThat(messages)
                .containsOnly(events);
    }
}
