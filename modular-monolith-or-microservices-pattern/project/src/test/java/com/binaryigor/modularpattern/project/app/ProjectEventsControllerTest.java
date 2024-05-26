package com.binaryigor.modularpattern.project.app;

import com.binaryigor.modularpattern.project.IntegrationTest;
import com.binaryigor.modularpattern.project.domain.ProjectUser;
import com.binaryigor.modularpattern.project.domain.ProjectUserRepository;
import com.binaryigor.modularpattern.project.domain.ProjectUsersSync;
import com.binaryigor.modularpattern.shared.contracts.UserChangedEvent;
import com.binaryigor.modularpattern.shared.contracts.UserView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class ProjectEventsControllerTest extends IntegrationTest {

    @Autowired
    private ProjectUsersSync projectUsersSync;
    @Autowired
    private ProjectUserRepository userRepository;

    @Test
    void returnsBadRequestWithInvalidUserChangedEvent() {
        var response = postEvent(UserChangedEvent.class.getSimpleName(), Map.of("unrecognizedField", "112"));
        Assertions.assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void ignoresValidUserChangedEventWithUnknownType() {
        var user = randomUserView();

        Assertions.assertThat(savedUserOfId(user.id())).isEmpty();

        var response = postEvent("UnknownType", new UserChangedEvent(user));

        Assertions.assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.OK);

        Assertions.assertThat(savedUserOfId(user.id())).isEmpty();
    }

    @Test
    void savesUsersOnExpectedUserChangedEvent() {
        var user1 = randomUserView();
        var user2 = randomUserView();
        var user1Changed = new UserView(user1.id(), user1.email() + "_", user1.name() + "_");

        assertUserSaved(new UserChangedEvent(user1));
        assertUserSaved(new UserChangedEvent(user2));
        assertUserSaved(new UserChangedEvent(user1Changed));
    }

    private ResponseEntity<Void> postEvent(String type, Object event) {
        return restTemplate.postForEntity("/events/" + type, event, Void.class);
    }

    private UserView randomUserView() {
        var id = UUID.randomUUID();
        return new UserView(id, id + "-email@email.com", id + "-name");
    }

    private Optional<ProjectUser> savedUserOfId(UUID id) {
        return Optional.ofNullable(userRepository.ofIds(List.of(id)).get(id));
    }

    private void assertUserSaved(UserChangedEvent eventToSend) {
        var response = postEvent(UserChangedEvent.class.getSimpleName(), eventToSend);

        Assertions.assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.OK);

        Assertions.assertThat(savedUserOfId(eventToSend.user().id()))
            .isNotEmpty()
            .get()
            .isEqualTo(ProjectUser.fromUserView(eventToSend.user()));
    }

}
