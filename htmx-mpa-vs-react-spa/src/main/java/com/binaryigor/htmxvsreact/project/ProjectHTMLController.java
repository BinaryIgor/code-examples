package com.binaryigor.htmxvsreact.project;

import com.binaryigor.htmxvsreact.shared.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.project.exception.ProjectValidationException;
import com.binaryigor.htmxvsreact.shared.HTMX;
import com.binaryigor.htmxvsreact.shared.Translations;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
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
        var projects = projectService.userProjects(userClient.currentUserId());
        var pageTranslations = Translations.messages("projects");
        return templates.renderPage("project/projects.mustache",
            Translations.enrich(Map.of("title", pageTranslations.get("title"),
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
            Translations.enrich(Map.of("title", Translations.message("create-project.title"),
                    "projectFormAttrs", "hx-post='/projects'",
                    "nameValue", "",
                    "nameErrorHidden", true,
                    "nameError", Translations.error(ProjectValidationException.class),
                    "submitValue", Translations.message("create-project.create")),
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

    @GetMapping("/{id}/edit")
    String editProjectPage(@PathVariable("id") UUID id) {
        var project = projectService.get(id, userClient.currentUserId());
        return templates.renderPage("project/edit-project.mustache",
            Translations.enrich(Map.of(
                    "project", project,
                    "title", "edit-project.title",
                    "projectFormAttrs", "hx-put='/projects/%s'".formatted(id),
                    "nameValue", project.name(),
                    "nameErrorHidden", true,
                    "nameError", Translations.error(ProjectValidationException.class),
                    "submitValue", Translations.message("edit-project.save")),
                "edit-project"));
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
            Translations.enrich(Map.of("project", project,
                    "title", project.name() + " " + Translations.message("project-page.project"),
                    "projectFormAttrs", "hx-put='/projects/%s'".formatted(id),
                    "nameValue", project.name(),
                    "nameErrorHidden", true,
                    "nameError", Translations.error(ProjectValidationException.class),
                    "submitValue", Translations.message("project-page.save")),
                "project-page"));
    }
}
