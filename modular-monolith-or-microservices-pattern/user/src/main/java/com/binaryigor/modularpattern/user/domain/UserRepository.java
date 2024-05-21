package com.binaryigor.modularpattern.user.domain;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    void save(User user);

    Optional<User> ofId(UUID id);

    Optional<User> ofEmail(String email);
}
