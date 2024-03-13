package com.binaryigor.smart.gateway;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

@RestController
public class ApisController {

    @GetMapping("/binary")
    public ResponseEntity<byte[]> getBinary() {
        return ResponseEntity.ok("Some bytes".getBytes(StandardCharsets.UTF_8));
    }

    @PostMapping("/user")
    public ResponseEntity<Void> createUser(@RequestBody User user) {
        System.out.println("Creating user!" + user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .build();
    }

    public record User(UUID id, String name) {
    }
}
