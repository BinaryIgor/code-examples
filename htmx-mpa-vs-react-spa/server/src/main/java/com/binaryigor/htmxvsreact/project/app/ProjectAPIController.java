package com.binaryigor.htmxvsreact.project.app;

import com.binaryigor.htmxvsreact.project.domain.Project;
import com.binaryigor.htmxvsreact.project.domain.ProjectService;
import com.binaryigor.htmxvsreact.project.domain.ProjectWithTasks;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectAPIController {

    private final ProjectService projectService;
    private final UserClient userClient;

    public ProjectAPIController(ProjectService projectService, UserClient userClient) {
        this.projectService = projectService;
        this.userClient = userClient;
    }

    @GetMapping
    List<ProjectWithTasks> projects() {
        return projectService.userProjects(userClient.currentUserId());
    }

    @GetMapping("/names")
    List<String> projectNames() {
        return projectService.userProjectNames(userClient.currentUserId());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    Project create(@RequestBody CreateProjectRequest request) {
        var project = Project.newOne(request.name(), userClient.currentUserId());
        projectService.create(project);
        return project;
    }

    @PutMapping("{id}")
    Project update(@PathVariable("id") UUID id, @RequestBody UpdateProjectRequest request) {
        var project = new Project(id, request.name(), userClient.currentUserId());
        projectService.update(project);
        return project;
    }

    @DeleteMapping("{id}")
    void delete(@PathVariable("id") UUID id) {
        projectService.delete(id, userClient.currentUserId());
    }

    @GetMapping("{id}")
    Project get(@PathVariable("id") UUID id) {
        return projectService.get(id, userClient.currentUserId());
    }

    record CreateProjectRequest(String name) {
    }

    record UpdateProjectRequest(String name) {
    }
}
