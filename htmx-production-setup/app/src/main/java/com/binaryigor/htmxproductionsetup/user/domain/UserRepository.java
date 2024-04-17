package com.binaryigor.htmxproductionsetup.user.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    void save(User user);

    Optional<User> ofEmail(String email);

    Optional<User> ofId(UUID id);
}
