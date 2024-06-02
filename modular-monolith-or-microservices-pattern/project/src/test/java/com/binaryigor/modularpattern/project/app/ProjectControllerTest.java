package com.binaryigor.modularpattern.project.app;

import com.binaryigor.modularpattern.project.IntegrationTest;
import com.binaryigor.modularpattern.project.TestObjects;
import com.binaryigor.modularpattern.project.domain.Project;
import com.binaryigor.modularpattern.project.domain.ProjectUser;
import com.binaryigor.modularpattern.project.domain.ProjectWithUsers;
import com.binaryigor.modularpattern.project.domain.exception.ProjectIdTakenException;
import com.binaryigor.modularpattern.project.domain.exception.ProjectUsersDoNotExistException;
import com.binaryigor.modularpattern.shared.contracts.UserChangedEvent;
import com.binaryigor.modularpattern.shared.contracts.UserView;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

public class ProjectControllerTest extends IntegrationTest {

    @Test
    void createsProjectAndReturnsItWithUsers() {
        var projectUsers = prepareUsers();
        var project = TestObjects.randomProject(projectUserIds(projectUsers));

        var createdProject = assertProjectCreated(project);

        var projectWithUsers = new ProjectWithUsers(createdProject, projectUsers);

        var getProjectResponse = getProject(createdProject.id());

        Assertions.assertThat(getProjectResponse.getStatusCode())
            .isEqualTo(HttpStatus.OK);
        Assertions.assertThat(getProjectResponse.getBody())
            .usingRecursiveComparison()
            .ignoringCollectionOrderInFields("users")
            .isEqualTo(projectWithUsers);
    }

    @Test
    void doesNotAllowToCreateProjectWithTakenId() {
        var projectUsers = prepareUsers();
        var project = TestObjects.randomProject(projectUserIds(projectUsers));

        var createdProject = assertProjectCreated(project);

        var response = createProjectExpectingException(createdProject);
        asserExceptionResponse(response, HttpStatus.CONFLICT, ProjectIdTakenException.class);
    }

    @Test
    void doesNotAllowToCreateProjectWithNonexistentUsers() {
        var project = TestObjects.randomProject(List.of(UUID.randomUUID(), UUID.randomUUID()));

        var response = createProjectExpectingException(project);

        asserExceptionResponse(response, HttpStatus.NOT_FOUND, ProjectUsersDoNotExistException.class);
    }

    private List<ProjectUser> prepareUsers() {
        var projectUsers = List.of(
            TestObjects.randomProjectUser(),
            TestObjects.randomProjectUser()
        );

        var userChangedEvents = projectUsers.stream()
            .map(pu -> new UserChangedEvent(new UserView(pu.id(), pu.email(), pu.name(), pu.version())))
            .toList();

        userChangedEvents.forEach(e -> appEvents.publisher().publish(e));

        return projectUsers;
    }

    private ResponseEntity<Project> createProject(Project project) {
        return restTemplate.postForEntity("/projects", project, Project.class);
    }

    private Project assertProjectCreated(Project project) {
        var response = createProject(project);

        Assertions.assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(response.getBody())
            .isEqualTo(project);

        return response.getBody();
    }

    private ResponseEntity<ProblemDetail> createProjectExpectingException(Project project) {
        return restTemplate.postForEntity("/projects", project, ProblemDetail.class);
    }

    private ResponseEntity<Project> updateProject(UUID id, UpdateProjectRequest request) {
        return restTemplate.exchange(RequestEntity.put("/projects/" + id).body(request), Project.class);
    }

    private ResponseEntity<ProjectWithUsers> getProject(UUID id) {
        return restTemplate.getForEntity("/projects/" + id, ProjectWithUsers.class);
    }

    private void asserExceptionResponse(ResponseEntity<ProblemDetail> response,
                                        HttpStatus status,
                                        Class<? extends Exception> exception) {
        Assertions.assertThat(response.getStatusCode()).isEqualTo(status);
        Assertions.assertThat(response.getBody().getType().toString())
            .isEqualTo(exception.getSimpleName());
    }

    private List<UUID> projectUserIds(List<ProjectUser> projectUsers) {
        return projectUsers.stream().map(ProjectUser::id).toList();
    }
}
