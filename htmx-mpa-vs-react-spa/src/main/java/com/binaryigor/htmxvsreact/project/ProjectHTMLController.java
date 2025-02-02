package com.binaryigor.htmxvsreact.project;

import com.binaryigor.htmxvsreact.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.project.exception.ProjectValidationException;
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
    private final ProjectService projectService;
    private final ProjectRepository projectRepository;
    private final UserClient userClient;

    public ProjectHTMLController(HTMLTemplates templates,
                                 ProjectService projectService,
                                 ProjectRepository projectRepository,
                                 UserClient userClient) {
        this.templates = templates;
        this.projectService = projectService;
        this.projectRepository = projectRepository;
        this.userClient = userClient;
    }

    @GetMapping
    String projectsPage() {
        var projects = projectRepository.userProjects(userClient.currentUserId());
        return templates.renderPage("project/projects.mustache",
            Map.of("title", Translations.message("projects.title"),
                "projectsTitle", Translations.message("projects.title"),
                "hasProjects", !projects.isEmpty(),
                "projects", projects,
                "confirmableModalId", "confirm-project-delete-modal",
                "confirmableModalTitle", Translations.message("projects.delete-modal-title"),
                "deleteProjectMessageTemplate", Translations.message("projects.delete-modal-content-template"),
                "confirmableModalLeft", Translations.message("projects.delete-modal-left"),
                "confirmableModalRight", Translations.message("projects.delete-modal-right")));
    }

    @GetMapping("/create")
    String createProjectPage() {
        return modifyProjectPage("project/create-project.mustache", "hx-post='/projects/create'",
            "", Translations.message("project.create"));
    }

    private String modifyProjectPage(String template, String formAttributes, String nameValue, String submitValue) {
        return templates.renderPage(template,
            Map.of("projectFormAttrs", formAttributes,
                "nameValue", nameValue,
                "nameErrorHidden", true,
                "nameError", Translations.error(ProjectValidationException.class),
                "submitValue", submitValue));
    }

    @PostMapping("/create")
    ResponseEntity<?> createProject(@RequestParam String name) {
        try {
            var project = Project.newOne(name, userClient.currentUserId());
            projectService.create(project);
        } catch (Exception e) {
            var projectForm = projectFormWithError("hx-post='/projects/create'", name, e,
                Translations.message("project.create"));
            return ResponseEntity.ok().body(projectForm);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .header("HX-Redirect", "/projects")
            .build();
    }

    private String projectFormWithError(String formAttributes,
                                        String name,
                                        Exception exception,
                                        String submitValue) {
        return templates.render("project/project-form.mustache",
            Map.of("projectFormAttrs", formAttributes,
                "nameValue", name,
                "nameErrorHidden", false,
                "nameError", Translations.error(exception),
                "submitValue", submitValue));
    }

    @GetMapping("/{id}/edit")
    String editProjectPage(@PathVariable("id") UUID id) {
        var project = projectService.get(id, userClient.currentUserId());
        return modifyProjectPage("project/edit-project.mustache", "hx-put='/projects/%s'".formatted(id),
            project.name(), Translations.message("project.save"));
    }

    @PutMapping("{id}")
    ResponseEntity<?> updateProject(@PathVariable("id") UUID id, @RequestParam String name) {
        try {
            var project = new Project(id, name, userClient.currentUserId());
            projectService.update(project);
        } catch (Exception e) {
            var projectForm = projectFormWithError("hx-put='/projects/%s'".formatted(id), name, e,
                Translations.message("project.save"));
            return ResponseEntity.ok().body(projectForm);
        }

        return ResponseEntity.ok()
            .header("HX-Redirect", "/projects/" + id)
            .build();
    }

    @DeleteMapping("{id}")
    void delete(@PathVariable("id") UUID id) {
        projectService.delete(id, userClient.currentUserId());
    }

    @GetMapping("{id}")
    String get(@PathVariable("id") UUID id) {
        var project = projectService.get(id, userClient.currentUserId());
        return templates.renderPage("project/project.mustache",
            Map.of("project", project));
    }
}
