package com.binaryigor.htmxvsreact.project.app;

import com.binaryigor.htmxvsreact.project.domain.Project;
import com.binaryigor.htmxvsreact.project.domain.ProjectService;
import com.binaryigor.htmxvsreact.shared.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.project.domain.exception.ProjectNameValidationException;
import com.binaryigor.htmxvsreact.shared.HTMX;
import com.binaryigor.htmxvsreact.shared.html.Translations;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@Hidden
@RestController
@RequestMapping("/projects")
public class ProjectHTMLController {

    private final HTMLTemplates templates;
    private final Translations translations;
    private final ProjectService projectService;
    private final UserClient userClient;

    public ProjectHTMLController(HTMLTemplates templates,
                                 Translations translations,
                                 ProjectService projectService,
                                 UserClient userClient) {
        this.templates = templates;
        this.translations = translations;
        this.projectService = projectService;
        this.userClient = userClient;
    }

    @GetMapping
    String projectsPage() {
        var projects = projectService.userProjects(userClient.currentUserId());
        var pageTranslations = translations.messages("projects");
        return templates.renderPage("project/projects.mustache",
            translations.enrich(Map.of("title", pageTranslations.get("title"),
                    "projectsTitle", pageTranslations.get("title"),
                    "hasProjects", !projects.isEmpty(),
                    "projects", projects,
                    "confirmableModalId", "confirm-project-delete-modal",
                    "confirmableModalTitle", pageTranslations.get("delete-modal-title"),
                    "deleteProjectMessageTemplate", pageTranslations.get("delete-modal-content-template"),
                    "confirmableModalLeft", pageTranslations.get("delete-modal-left"),
                    "confirmableModalRight", pageTranslations.get("delete-modal-right")),
                "projects"));
    }

    @GetMapping("/create")
    String createProjectPage() {
        return templates.renderPage("project/create-project.mustache",
            translations.enrich(Map.of("title", translations.message("create-project.title"),
                    "projectFormAttrs", "hx-post='/projects'",
                    "namePlaceholder", translations.message("create-project.name-placeholder"),
                    "nameValue", "",
                    "nameError", translations.error(ProjectNameValidationException.class),
                    "submitValue", translations.message("create-project.create")),
                "create-project"));
    }

    @PostMapping
    ResponseEntity<?> createProject(@RequestParam String name) {
        var project = Project.newOne(name, userClient.currentUserId());
        projectService.create(project);
        return ResponseEntity.status(HttpStatus.CREATED)
            .header(HTMX.REDIRECT_HEADER, "/projects")
            .build();
    }

    @PutMapping("{id}")
    ResponseEntity<?> updateProject(@PathVariable("id") UUID id, @RequestParam String name) {
        var project = new Project(id, name, userClient.currentUserId());
        projectService.update(project);
        return ResponseEntity.ok()
            .header(HTMX.REDIRECT_HEADER, "/projects/" + id)
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
            translations.enrich(Map.of("project", project,
                    "title", project.name() + " " + translations.message("project-page.project"),
                    "projectFormAttrs", "hx-put='/projects/%s'".formatted(id),
                    "namePlaceholder", translations.message("project-page.name-placeholder"),
                    "nameValue", project.name(),
                    "nameError", translations.error(ProjectNameValidationException.class),
                    "submitValue", translations.message("project-page.save")),
                "project-page"));
    }
}
