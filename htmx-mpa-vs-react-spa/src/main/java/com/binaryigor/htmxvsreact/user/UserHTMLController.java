package com.binaryigor.htmxvsreact.user;

import com.binaryigor.htmxvsreact.shared.HTMX;
import com.binaryigor.htmxvsreact.shared.Translations;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import com.binaryigor.htmxvsreact.shared.html.HTMLTemplates;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserHTMLController {

    private final HTMLTemplates templates;
    private final UserClient userClient;

    public UserHTMLController(HTMLTemplates templates, UserClient userClient) {
        this.templates = templates;
        this.userClient = userClient;
    }

    @GetMapping("/sign-in")
    String signInPage() {
        return templates.renderPage("user/sign-in.mustache",
            Translations.enrich(Map.of(
                    "title", Translations.message("sign-in.title"),
                    "hideNavigation", true),
                "sign-in"));
    }

    @PostMapping("/sign-in")
    ResponseEntity<?> signIn(@RequestParam String email,
                             @RequestParam String password) {
        // TODO: impl
        return ResponseEntity.ok()
            .header(HTMX.TRIGGER_HEADER, """
                {"replace-url": "/projects"}""")
            .build();
    }

    @GetMapping("/account")
    String account() {
        return templates.renderPage("user/account.mustache",
            Translations.enrich(Map.of("title", Translations.message("user-account.title"),
                    "user", userClient.currentUser()),
                "user-account"));
    }
}
