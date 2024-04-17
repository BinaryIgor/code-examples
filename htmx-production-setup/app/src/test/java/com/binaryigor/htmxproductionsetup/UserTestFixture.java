package com.binaryigor.htmxproductionsetup;

import com.binaryigor.htmxproductionsetup.shared.contracts.UserData;
import com.binaryigor.htmxproductionsetup.user.domain.PasswordHasher;
import com.binaryigor.htmxproductionsetup.user.domain.User;
import com.binaryigor.htmxproductionsetup.user.domain.UserRepository;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class UserTestFixture {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final TestRestTemplate restTemplate;

    // Aware of low-level repository and hasher only because we don't have the sign-up procedure!
    // It's only a demo, proof of concept in the end!
    public UserTestFixture(UserRepository userRepository,
                           PasswordHasher passwordHasher,
                           TestRestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.restTemplate = restTemplate;
    }

    public void createUser(UserData user,
                           String password) {
        userRepository.save(new User(user.id(), user.email(), user.name(),
                passwordHasher.hash(password),
                user.language()));
    }

    public ResponseEntity<String> signIn(String email, String password) {
        return HttpTests.postForm(restTemplate, "/sign-in",
                Map.of("email", email,
                        "password", password));
    }
}
