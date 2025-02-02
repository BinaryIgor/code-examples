package com.binaryigor.htmxvsreact.task;

import com.binaryigor.htmxvsreact.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.shared.Translations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/tasks")
public class TaskHTMLController {

    private final HTMLTemplates templates;

    public TaskHTMLController(HTMLTemplates templates) {
        this.templates = templates;
    }

    @GetMapping
    String tasks() {
        return templates.renderPage("task/tasks.mustache",
            Map.of("title", Translations.message("tasks.title")));
    }
}
