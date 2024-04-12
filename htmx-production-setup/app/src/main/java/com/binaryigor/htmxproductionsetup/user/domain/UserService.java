package com.binaryigor.htmxproductionsetup.user.domain;

import com.binaryigor.htmxproductionsetup.shared.contracts.AuthClient;
import com.binaryigor.htmxproductionsetup.shared.exception.NotFoundException;
import com.binaryigor.htmxproductionsetup.user.domain.exception.IncorrectPasswordException;
import com.binaryigor.htmxproductionsetup.user.domain.exception.InvalidEmailException;
import com.binaryigor.htmxproductionsetup.user.domain.exception.InvalidPasswordException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AuthClient authClient;

    public UserService(UserRepository userRepository,
                       PasswordHasher passwordHasher,
                       AuthClient authClient) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.authClient = authClient;
    }

    public SignedInUser signIn(SignInRequest request) {
        validateRequest(request);

        var user = userRepository.ofEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User"));

        if (!passwordHasher.matches(request.password(), user.password())) {
            throw new IncorrectPasswordException();
        }

        return new SignedInUser(user.id(), user.email(), user.name(),
                authClient.ofUser(user.id()));
    }

    private void validateRequest(SignInRequest request) {
        if (!UserValidator.isEmailValid(request.email())) {
            throw new InvalidEmailException(request.email());
        }
        if (!UserValidator.isPasswordValid(request.password())) {
            throw new InvalidPasswordException();
        }
    }

    public User userOfId(UUID id) {
        return userRepository.ofId(id).orElseThrow(() -> new NotFoundException("User"));
    }
}
