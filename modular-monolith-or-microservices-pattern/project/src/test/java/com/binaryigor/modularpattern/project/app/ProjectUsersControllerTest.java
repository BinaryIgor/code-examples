package com.binaryigor.modularpattern.project.app;

import com.binaryigor.modularpattern.project.IntegrationTest;
import com.binaryigor.modularpattern.project.TestObjects;
import com.binaryigor.modularpattern.project.domain.ProjectUser;
import com.binaryigor.modularpattern.shared.NdJson;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Stream;

public class ProjectUsersControllerTest extends IntegrationTest {

    @Test
    void synchronizesAllUsersAndReturnsThem() {
        Assertions.assertThat(getAllProjectUsers())
            .isEmpty();

        var allUsers = Stream.generate(TestObjects::randomUserView)
            .limit(25)
            .toList();

        userClient.addUsers(allUsers);

        var syncAllUsersResponse = syncAllUsers();

        Assertions.assertThat(syncAllUsersResponse.getStatusCode())
            .isEqualTo(HttpStatus.OK);

        var expectedProjectUsers = allUsers.stream().map(ProjectUser::fromUserView).toList();

        Assertions.assertThat(getAllProjectUsers())
            .containsExactlyElementsOf(expectedProjectUsers);
    }


    private ResponseEntity<Void> syncAllUsers() {
        return restTemplate.postForEntity("/project-users/sync-all", null, Void.class);
    }

    private List<ProjectUser> getAllProjectUsers() {
        return restTemplate.execute("/project-users", HttpMethod.GET,
            request -> {
            },
            response -> allProjectUsersFromInputStream(response.getBody()));
    }

    private List<ProjectUser> allProjectUsersFromInputStream(InputStream input) {
        try {
            var ndJson = new String(input.readAllBytes());
            return NdJson.readFrom(ndJson, objectMapper, ProjectUser.class).toList();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
