package com.binaryigor.modularpattern.user.app;

import com.binaryigor.modularpattern.user.domain.User;
import com.binaryigor.modularpattern.user.domain.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService service;

    public UserController(UserService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    User create(@RequestBody User user) {
        service.create(user);
        return user;
    }

    @PutMapping("{id}")
    User update(@PathVariable("id") UUID id,
                @RequestBody UpdateUserRequest request) {
        var user = request.toUser(id);
        service.update(user);
        return user;
    }

    @GetMapping("{id}")
    User get(@PathVariable("id") UUID id) {
        return service.ofId(id);
    }
}
