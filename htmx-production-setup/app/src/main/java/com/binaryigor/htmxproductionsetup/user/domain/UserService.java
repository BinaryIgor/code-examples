package com.binaryigor.htmxproductionsetup.user.domain;

import com.binaryigor.htmxproductionsetup.shared.contracts.AuthApi;
import com.binaryigor.htmxproductionsetup.shared.exception.NotFoundException;
import com.binaryigor.htmxproductionsetup.user.domain.exception.IncorrectPasswordException;
import com.binaryigor.htmxproductionsetup.user.domain.exception.InvalidEmailException;
import com.binaryigor.htmxproductionsetup.user.domain.exception.InvalidPasswordException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final AuthApi authApi;

    public UserService(UserRepository userRepository,
                       PasswordHasher passwordHasher,
                       AuthApi authApi) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.authApi = authApi;
    }

    public SignedInUser signIn(SignInRequest request) {
        validateEmail(request.email());
        validatePassword(request.password());

        var user = userRepository.ofEmail(request.email())
                .orElseThrow(() -> new NotFoundException("User"));

        if (!passwordHasher.matches(request.password(), user.password())) {
            throw new IncorrectPasswordException();
        }

        return new SignedInUser(user.id(), user.email(), user.name(),
                authApi.tokenOfUser(user.id()));
    }

    public void validateEmail(String email) {
        if (!UserValidator.isEmailValid(email)) {
            throw new InvalidEmailException(email);
        }
    }

    public void validatePassword(String password) {
        if (!UserValidator.isPasswordValid(password)) {
            throw new InvalidPasswordException();
        }
    }

}
