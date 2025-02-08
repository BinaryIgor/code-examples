package com.binaryigor.htmxvsreact.user.domain;

import com.binaryigor.htmxvsreact.user.domain.exception.UserDoesNotExistException;
import com.binaryigor.htmxvsreact.user.domain.exception.UserEmailException;
import com.binaryigor.htmxvsreact.user.domain.exception.UserIncorrectPasswordException;
import com.binaryigor.htmxvsreact.user.domain.exception.UserPasswordException;

import java.util.UUID;

public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public UserService(UserRepository userRepository, PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    public User signIn(String email, String password) {
        if (!UserValidator.isEmailValid(email)) {
            throw new UserEmailException();
        }
        if (!UserValidator.isPasswordValid(password)) {
            throw new UserPasswordException();
        }
        var user = userRepository.ofEmail(email).orElseThrow(() -> UserDoesNotExistException.ofEmail(email));
        if (!passwordHasher.matches(password, user.password())) {
            throw new UserIncorrectPasswordException();
        }
        return user;
    }

    public User user(UUID id) {
        return userRepository.ofId(id).orElseThrow(() -> UserDoesNotExistException.ofId(id));
    }
}
