package com.binaryigor.htmxproductionsetup.user.domain;

import com.binaryigor.htmxproductionsetup.shared.contracts.UserApi;
import com.binaryigor.htmxproductionsetup.shared.contracts.UserData;
import com.binaryigor.htmxproductionsetup.shared.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class TheUserApi implements UserApi {

    private final UserRepository userRepository;

    public TheUserApi(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserData userOfId(UUID id) {
        return userRepository.ofId(id)
                .map(u -> new UserData(u.id(), u.email(), u.name(), u.language()))
                .orElseThrow(() -> new NotFoundException("User"));
    }
}
