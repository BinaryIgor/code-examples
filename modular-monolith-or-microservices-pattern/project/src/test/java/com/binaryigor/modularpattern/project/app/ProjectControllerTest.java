package com.binaryigor.modularpattern.project.app;

import com.binaryigor.modularpattern.project.IntegrationTest;
import com.binaryigor.modularpattern.project.domain.Project;
import com.binaryigor.modularpattern.project.domain.ProjectUser;
import com.binaryigor.modularpattern.project.domain.ProjectWithUsers;
import com.binaryigor.modularpattern.shared.contracts.UserChangedEvent;
import com.binaryigor.modularpattern.shared.contracts.UserView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public class ProjectControllerTest extends IntegrationTest {

    @Test
    void createsProjectAndReturnsItWithUsers() {
        var projectUsers = prepareUsers();
        var project = new Project(UUID.randomUUID(),
            "some-namespace",
            "some-name",
            null,
            projectUsers.stream().map(ProjectUser::id).toList());

        var createProjectResponse = createProject(project);

        Assertions.assertThat(createProjectResponse.getStatusCode())
            .isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(createProjectResponse.getBody())
            .isEqualTo(project);

        var projectWithUsers = new ProjectWithUsers(project, projectUsers);

        var getProjectResponse = getProject(project.id());

        Assertions.assertThat(getProjectResponse.getStatusCode())
            .isEqualTo(HttpStatus.OK);
        Assertions.assertThat(getProjectResponse.getBody())
            .usingRecursiveComparison()
            .ignoringCollectionOrderInFields("users")
            .isEqualTo(projectWithUsers);
    }

    private List<ProjectUser> prepareUsers() {
        var projectUsers = List.of(
            new ProjectUser(UUID.randomUUID(), "user1@email.com", "user1"),
            new ProjectUser(UUID.randomUUID(), "user2@email.com", "user2")
        );

        var userChangedEvents = projectUsers.stream()
            .map(pu -> new UserChangedEvent(new UserView(pu.id(), pu.email(), pu.name())))
            .toList();

        userChangedEvents.forEach(e -> appEvents.publisher().publish(e));

        return projectUsers;
    }

    private ResponseEntity<Project> createProject(Project project) {
        return restTemplate.postForEntity("/projects", project, Project.class);
    }

    private ResponseEntity<Project> updateProject(UUID id, UpdateProjectRequest request) {
        return restTemplate.exchange(RequestEntity.put("/projects/" + id).body(request), Project.class);
    }

    private ResponseEntity<ProjectWithUsers> getProject(UUID id) {
        return restTemplate.getForEntity("/projects/" + id, ProjectWithUsers.class);
    }
}
