package com.binaryigor.modularpattern.user.app;

import com.binaryigor.modularpattern.shared.NdJson;
import com.binaryigor.modularpattern.shared.contracts.UserChangedEvent;
import com.binaryigor.modularpattern.shared.contracts.UserView;
import com.binaryigor.modularpattern.shared.outbox.OutboxMessage;
import com.binaryigor.modularpattern.user.IntegrationTest;
import com.binaryigor.modularpattern.user.TestObjects;
import com.binaryigor.modularpattern.user.domain.User;
import com.binaryigor.modularpattern.user.domain.exception.UserDoesNotExistException;
import com.binaryigor.modularpattern.user.domain.exception.UserEmailTakenException;
import com.binaryigor.modularpattern.user.domain.exception.UserIdTakenException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class UserControllerTest extends IntegrationTest {

    @Test
    void createsUpdatesAndGetsUser() {
        var newUser = TestObjects.randomNewUser();

        var createdUser = assertUserCreated(newUser);

        var userId = newUser.id();

        var updateUserRequest = new UpdateUserRequest("some-email2@email.com", "some-name-2");

        var updateUserResponse = updateUser(userId, updateUserRequest);

        var expectedUserAfterUpdate = updateUserRequestToUser(userId, updateUserRequest, 2);

        Assertions.assertThat(updateUserResponse.getStatusCode())
            .isEqualTo(HttpStatus.OK);
        Assertions.assertThat(updateUserResponse.getBody())
            .isEqualTo(expectedUserAfterUpdate);

        var getUserResponse = getUser(userId);
        Assertions.assertThat(getUserResponse.getStatusCode())
            .isEqualTo(HttpStatus.OK);
        Assertions.assertThat(getUserResponse.getBody())
            .isEqualTo(expectedUserAfterUpdate);

        var userCreatedEvent = createdUser.toUserChangedEvent();
        var userUpdatedEvent = updateUserResponse.getBody().toUserChangedEvent();

        assertUserChangedEventsWerePublishedToOutbox(userCreatedEvent, userUpdatedEvent);
    }

    @Test
    void doesNotAllowToCreateUserWithTakenId() {
        var user = assertUserCreated(TestObjects.randomNewUser());

        var response = createUserExpectingException(user);
        asserExceptionResponse(response, HttpStatus.CONFLICT, UserIdTakenException.class);
    }

    @Test
    void doesNotAllowToCreateUserWithTakenEmail() {
        var user = assertUserCreated(TestObjects.randomNewUser());

        var anotherUser = TestObjects.randomNewUser();
        var anotherUserWithTakenEmail = new User(anotherUser.id(), user.email(), anotherUser.name(), anotherUser.version());

        var response = createUserExpectingException(anotherUserWithTakenEmail);
        asserExceptionResponse(response, HttpStatus.CONFLICT, UserEmailTakenException.class);
    }

    @Test
    void returns404WhenTryingToUpdateNonexistentUser() {
        var id = UUID.randomUUID();
        var request = new UpdateUserRequest("some-email@email.com", "some-name");

        var response = updateUserExpectingException(id, request);

        asserExceptionResponse(response, HttpStatus.NOT_FOUND, UserDoesNotExistException.class);
    }

    @Test
    void returnsStreamOfAllUsers() {
        var users = Stream.generate(TestObjects::randomNewUser)
            .limit(50)
            .toList();

        users.forEach(this::createUser);

        var expectedUsers = users.stream().map(User::toView).toList();

        Assertions.assertThat(getAllUsers())
            .containsExactlyElementsOf(expectedUsers);
    }


    private ResponseEntity<User> createUser(User user) {
        return restTemplate.postForEntity("/users", user, User.class);
    }

    private User assertUserCreated(User user) {
        var response = createUser(user);

        Assertions.assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(response.getBody())
            .isEqualTo(TestObjects.userWithIncreasedVersion(user));

        return response.getBody();
    }

    private ResponseEntity<ProblemDetail> createUserExpectingException(User user) {
        return restTemplate.postForEntity("/users", user, ProblemDetail.class);
    }

    private ResponseEntity<User> updateUser(UUID id, UpdateUserRequest request) {
        return restTemplate.exchange(RequestEntity.put("/users/" + id).body(request), User.class);
    }

    private ResponseEntity<ProblemDetail> updateUserExpectingException(UUID id, UpdateUserRequest request) {
        return restTemplate.exchange(RequestEntity.put("/users/" + id).body(request), ProblemDetail.class);
    }

    private ResponseEntity<User> getUser(UUID id) {
        return restTemplate.getForEntity("/users/" + id, User.class);
    }

    private List<UserView> getAllUsers() {
        return restTemplate.execute("/users", HttpMethod.GET,
            request -> {
            },
            response -> allUsersFromInputStream(response.getBody()));
    }

    private List<UserView> allUsersFromInputStream(InputStream input) {
        try {
            var ndJson = new String(input.readAllBytes());
            return NdJson.readFrom(ndJson, objectMapper, UserView.class).toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private User updateUserRequestToUser(UUID id, UpdateUserRequest request, long version) {
        return new User(id, request.email(), request.name(), version);
    }

    private void assertUserChangedEventsWerePublishedToOutbox(UserChangedEvent... events) {
        var messages = userOutboxRepository.all().stream().map(OutboxMessage::message).toList();
        Assertions.assertThat(messages)
            .containsOnly(events);
    }

    private void asserExceptionResponse(ResponseEntity<ProblemDetail> response,
                                        HttpStatus status,
                                        Class<? extends Exception> exception) {
        Assertions.assertThat(response.getStatusCode()).isEqualTo(status);
        Assertions.assertThat(response.getBody().getType().toString())
            .isEqualTo(exception.getSimpleName());
    }
}
