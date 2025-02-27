package com.binaryigor.htmxvsreact.user.domain;

import com.binaryigor.htmxvsreact.user.domain.exception.UserDoesNotExistException;
import com.binaryigor.htmxvsreact.user.domain.exception.UserEmailValidationException;
import com.binaryigor.htmxvsreact.user.domain.exception.UserIncorrectPasswordException;
import com.binaryigor.htmxvsreact.user.domain.exception.UserPasswordValidationException;

import java.util.UUID;

public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AuthTokenCreator authTokenCreator;

    public UserService(UserRepository userRepository, PasswordHasher passwordHasher, AuthTokenCreator authTokenCreator) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.authTokenCreator = authTokenCreator;
    }

    public SignedInUser signIn(String email, String password) {
        if (!UserValidator.isEmailValid(email)) {
            throw new UserEmailValidationException();
        }
        if (!UserValidator.isPasswordValid(password)) {
            throw new UserPasswordValidationException();
        }
        var user = userRepository.ofEmail(email).orElseThrow(() -> UserDoesNotExistException.ofEmail(email));
        if (!passwordHasher.matches(password, user.password())) {
            throw new UserIncorrectPasswordException();
        }

        return new SignedInUser(user, authTokenCreator.ofUser(user.id()));
    }

    public User user(UUID id) {
        return userRepository.ofId(id).orElseThrow(() -> UserDoesNotExistException.ofId(id));
    }
}
