package com.binaryigor.htmxvsreact.user.app;

import com.binaryigor.htmxvsreact.shared.AppLanguage;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import com.binaryigor.htmxvsreact.user.domain.AuthTokenCreator;
import com.binaryigor.htmxvsreact.user.domain.UserService;
import com.binaryigor.htmxvsreact.user.infra.web.Cookies;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.UUID;

@RestController
public class UserAPIController {

    private final UserService userService;
    private final UserClient userClient;
    private final AuthTokenCreator authTokenCreator;
    private final Cookies cookies;

    public UserAPIController(UserService userService,
                             UserClient userClient,
                             AuthTokenCreator authTokenCreator,
                             Cookies cookies) {
        this.userService = userService;
        this.userClient = userClient;
        this.authTokenCreator = authTokenCreator;
        this.cookies = cookies;
    }

    @PostMapping("/api/sign-in")
    void signIn(@RequestBody SignInRequest request, HttpServletResponse response) {
        var signedInUser = userService.signIn(request.email(), request.password());
        var authToken = authTokenCreator.ofUser(signedInUser.id());
        response.addCookie(cookies.token(authToken.value(), authToken.expiresAt()));
    }

    @GetMapping("/api/sign-out")
    void signOut(HttpServletResponse response) throws IOException {
        response.addCookie(cookies.expiredToken());
        response.sendRedirect("/sign-in");
    }

    @GetMapping("/api/user-info")
    ResponseEntity<UserInfo> userInfo() {
        return userClient.currentUserIdOpt()
            .map(id -> {
                var user = userService.user(id);
                return ResponseEntity.ok()
                    .body(new UserInfo(id, user.email(), user.name(), user.language()));
            })
            .orElseGet(() -> ResponseEntity.noContent().build());
    }

    record SignInRequest(String email, String password) {
    }

    record UserInfo(UUID id, String email, String name, AppLanguage language) {
    }
}
