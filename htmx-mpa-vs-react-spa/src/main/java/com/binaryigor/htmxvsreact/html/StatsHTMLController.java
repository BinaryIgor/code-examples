package com.binaryigor.htmxvsreact.html;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/stats")
public class StatsHTMLController {

    private final HTMLTemplates templates;

    public StatsHTMLController(HTMLTemplates templates) {
        this.templates = templates;
    }

    @GetMapping
    String stats() {
        return templates.renderPage("stats.mustache", Map.of("title", "Stats"));
    }
}
