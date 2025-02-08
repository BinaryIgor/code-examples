package com.binaryigor.htmxvsreact.user.app;

import com.binaryigor.htmxvsreact.shared.HTMX;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import com.binaryigor.htmxvsreact.shared.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.shared.html.Translations;
import com.binaryigor.htmxvsreact.user.domain.AuthTokenCreator;
import com.binaryigor.htmxvsreact.user.domain.UserService;
import com.binaryigor.htmxvsreact.user.domain.exception.UserEmailException;
import com.binaryigor.htmxvsreact.user.domain.exception.UserPasswordException;
import com.binaryigor.htmxvsreact.user.infra.web.Cookies;
import io.swagger.v3.oas.annotations.Hidden;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@Hidden
@RestController
public class UserHTMLController {

    private final HTMLTemplates templates;
    private final Translations translations;
    private final UserService userService;
    private final UserClient userClient;
    private final AuthTokenCreator authTokenCreator;
    private final Cookies cookies;

    public UserHTMLController(HTMLTemplates templates,
                              Translations translations,
                              UserService userService,
                              UserClient userClient,
                              AuthTokenCreator authTokenCreator,
                              Cookies cookies) {
        this.templates = templates;
        this.translations = translations;
        this.userService = userService;
        this.userClient = userClient;
        this.authTokenCreator = authTokenCreator;
        this.cookies = cookies;
    }

    @GetMapping("/sign-in")
    String signInPage() {
        return templates.renderPage("user/sign-in.mustache",
            translations.enrich(Map.of(
                    "title", translations.message("sign-in.title"),
                    "hideNavigation", true,
                    "emailError", translations.error(UserEmailException.class),
                    "passwordError", translations.error(UserPasswordException.class)),
                "sign-in"));
    }

    @PostMapping("/sign-in")
    ResponseEntity<?> signIn(@RequestParam String email,
                             @RequestParam String password,
                             HttpServletResponse response) {
        var signedInUser = userService.signIn(email, password);
        var authToken = authTokenCreator.ofUser(signedInUser.id());

        response.addCookie(cookies.token(authToken.value(), authToken.expiresAt()));

        return ResponseEntity.ok()
            .header(HTMX.REDIRECT_HEADER, "/projects")
            .build();
    }

    @GetMapping("/sign-out")
    void signOut(HttpServletResponse response) throws IOException {
        response.addCookie(cookies.expiredToken());
        response.sendRedirect("/sign-in");
    }

    @GetMapping("/account")
    String account() {
        return templates.renderPage("user/account.mustache",
            translations.enrich(Map.of("title", translations.message("user-account.title"),
                    "user", userService.user(userClient.currentUserId())),
                "user-account"));
    }
}
