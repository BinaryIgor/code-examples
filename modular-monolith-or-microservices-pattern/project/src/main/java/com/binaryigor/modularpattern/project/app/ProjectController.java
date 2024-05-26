package com.binaryigor.modularpattern.project.app;

import com.binaryigor.modularpattern.project.domain.Project;
import com.binaryigor.modularpattern.project.domain.ProjectService;
import com.binaryigor.modularpattern.project.domain.ProjectWithUsers;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService service;

    public ProjectController(ProjectService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Project create(@RequestBody Project project) {
        service.create(project);
        return project;
    }

    @PutMapping("{id}")
    Project update(@PathVariable("id") UUID id,
                   @RequestBody UpdateProjectRequest request) {
        var project = request.toProject(id);
        service.update(project);
        return project;
    }

    @GetMapping("{id}")
    ProjectWithUsers ofId(@PathVariable("id") UUID id) {
        return service.ofId(id);
    }

    @GetMapping
    List<ProjectWithUsers> allOfNamespaceWithUsers(@RequestParam("namespace") String namespace) {
        return service.allOfNamespace(namespace);
    }
}
