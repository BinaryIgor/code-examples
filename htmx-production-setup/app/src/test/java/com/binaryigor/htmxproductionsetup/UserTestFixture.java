package com.binaryigor.htmxproductionsetup;

import com.binaryigor.htmxproductionsetup.shared.contracts.UserData;
import com.binaryigor.htmxproductionsetup.user.domain.User;
import com.binaryigor.htmxproductionsetup.user.domain.UserRepository;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class UserTestFixture {

    private final UserRepository userRepository;
    private final TestRestTemplate restTemplate;

    public UserTestFixture(UserRepository userRepository,
                           TestRestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    public void createUser(UserData user,
                           String password) {
        userRepository.save(new User(user.id(), user.email(), user.name(), password, user.language()));
    }

    public ResponseEntity<String> signIn(String email, String password) {
        return HttpTests.postForm(restTemplate, "/sign-in",
                Map.of("email", email,
                        "password", password));
    }
}
