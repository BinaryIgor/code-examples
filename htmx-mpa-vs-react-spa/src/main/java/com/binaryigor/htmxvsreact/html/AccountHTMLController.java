package com.binaryigor.htmxvsreact.html;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/account")
public class AccountHTMLController {

    private final HTMLTemplates templates;

    public AccountHTMLController(HTMLTemplates templates) {
        this.templates = templates;
    }

    @GetMapping
    String account() {
        return templates.renderPage("account.mustache", Map.of("title", "Account"));
    }
}
