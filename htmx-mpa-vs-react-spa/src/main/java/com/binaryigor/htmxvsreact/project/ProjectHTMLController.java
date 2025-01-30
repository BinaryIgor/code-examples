package com.binaryigor.htmxvsreact.project;

import com.binaryigor.htmxvsreact.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.shared.Translations;
import com.binaryigor.htmxvsreact.shared.UserClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
public class ProjectHTMLController {

    private final HTMLTemplates templates;
    private final ProjectRepository projectRepository;
    private final UserClient userClient;

    public ProjectHTMLController(HTMLTemplates templates, ProjectRepository projectRepository, UserClient userClient) {
        this.templates = templates;
        this.projectRepository = projectRepository;
        this.userClient = userClient;
    }

    @GetMapping
    String projectsPage() {
        var projects = projectRepository.userProjects(userClient.currentUserId());
        return templates.renderPage("project/projects.mustache",
            Map.of("title", "Projects",
                "hasProjects", !projects.isEmpty(),
                "projects", projects,
                "confirmableModalId", "confirm-project-delete-modal"));
    }

    @GetMapping("/create")
    String createProjectPage() {
        return templates.renderPage("project/create-project.mustache",
            Map.of("nameErrorHidden", true,
                "nameError", Translations.error(ProjectValidationException.class)));
    }

    @PostMapping("/create")
    ResponseEntity<?> createProject(@RequestParam String name) {
        System.out.println("Creating project with of name: " + name);

        try {
            var project = Project.newOne(name, userClient.currentUserId());
            projectRepository.save(project);
        } catch (Exception e) {
            var error = Translations.error(e);
            var projectForm = templates.renderPartial("project/project-form.mustache",
                Map.of("nameValue", name,
                    "nameErrorHidden", false,
                    "nameError", error));
            return ResponseEntity.ok().body(projectForm);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .header("HX-Redirect", "/projects")
            .build();
    }

    // TODO: validation
    @DeleteMapping("{id}")
    void delete(@PathVariable("id") UUID id) {
        projectRepository.delete(id);
    }
}
