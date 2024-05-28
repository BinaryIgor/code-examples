package com.binaryigor.modularpattern.user.app;

import com.binaryigor.modularpattern.shared.NdJson;
import com.binaryigor.modularpattern.user.domain.User;
import com.binaryigor.modularpattern.user.domain.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.UUID;

@UserControllerTag
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;
    private final ObjectMapper objectMapper;

    public UserController(UserService service,
                          ObjectMapper objectMapper) {
        this.service = service;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    User create(@RequestBody CreateUserRequest request) {
        return service.create(request.toChangeCommand());
    }

    @PutMapping("{id}")
    User update(@PathVariable("id") UUID id,
                @RequestBody UpdateUserRequest request) {
        return service.update(request.toChangeCommand(id));
    }

    @GetMapping("{id}")
    User get(@PathVariable("id") UUID id) {
        return service.ofId(id);
    }

    @GetMapping
    ResponseEntity<StreamingResponseBody> allUsers() {
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_NDJSON)
            .body(out -> NdJson.writeTo(service.allUsers(), objectMapper, out));
    }
}
