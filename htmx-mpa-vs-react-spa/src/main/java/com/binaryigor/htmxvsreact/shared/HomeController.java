package com.binaryigor.htmxvsreact.shared;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

// TODO: proper implementation
@RestController
public class HomeController {

    @GetMapping("/")
    ResponseEntity<String> handle() {
        // TODO: check if signed-in
        return ResponseEntity.status(HttpStatus.SEE_OTHER)
            .location(URI.create("/projects"))
            .build();
    }
}
