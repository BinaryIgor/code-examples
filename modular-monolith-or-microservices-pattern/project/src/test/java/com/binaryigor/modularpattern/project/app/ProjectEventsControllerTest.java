package com.binaryigor.modularpattern.project.app;

import com.binaryigor.modularpattern.project.IntegrationTest;
import com.binaryigor.modularpattern.project.TestObjects;
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
        var user = TestObjects.randomUserView();

        Assertions.assertThat(savedUserOfId(user.id())).isEmpty();

        var response = postEvent("UnknownType", new UserChangedEvent(user));

        Assertions.assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.OK);

        Assertions.assertThat(savedUserOfId(user.id())).isEmpty();
    }

    @Test
    void savesUsersOnExpectedUserChangedEventIfNewUserOrVersionIsGreaterOrEqualThanTheCurrentOne() {
        var user1Id = UUID.randomUUID();
        var user1 = TestObjects.randomUserView(user1Id, 1);
        var user1Changed = TestObjects.randomUserView(user1Id, 3);
        var user1ChangedToIgnoreVersion = TestObjects.randomUserView(user1Id, 2);

        var user2Id = UUID.randomUUID();
        var user2 = TestObjects.randomUserView(user2Id, 2);

        assertUserSaved(new UserChangedEvent(user1), user1);
        assertUserSaved(new UserChangedEvent(user1Changed), user1Changed);
        assertUserSaved(new UserChangedEvent(user1ChangedToIgnoreVersion), user1Changed);

        assertUserSaved(new UserChangedEvent(user2), user2);
    }

    private ResponseEntity<Void> postEvent(String type, Object event) {
        return restTemplate.postForEntity("/events/" + type, event, Void.class);
    }

    private Optional<ProjectUser> savedUserOfId(UUID id) {
        return Optional.ofNullable(userRepository.ofIds(List.of(id)).get(id));
    }

    private void assertUserSaved(UserChangedEvent toSendEvent, UserView expectedUser) {
        var response = postEvent(UserChangedEvent.class.getSimpleName(), toSendEvent);
        Assertions.assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.OK);

        Assertions.assertThat(savedUserOfId(expectedUser.id()))
            .isNotEmpty()
            .get()
            .isEqualTo(ProjectUser.fromUserView(expectedUser));
    }

}
