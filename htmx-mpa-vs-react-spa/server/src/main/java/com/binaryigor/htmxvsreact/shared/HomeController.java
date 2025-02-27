package com.binaryigor.htmxvsreact.shared;

import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Hidden
@RestController
public class HomeController {

    private final UserClient userClient;

    public HomeController(UserClient userClient) {
        this.userClient = userClient;
    }

    @GetMapping("/")
    ResponseEntity<String> handle() {
        var location = userClient.currentUserIdOpt().isPresent() ? "/projects" : "/sign-in";
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
            .location(URI.create(location))
            .build();
    }
}
