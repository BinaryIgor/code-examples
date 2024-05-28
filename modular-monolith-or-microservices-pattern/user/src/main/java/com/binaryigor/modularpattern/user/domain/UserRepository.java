package com.binaryigor.modularpattern.user.domain;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface UserRepository {

    User save(User user);

    Optional<User> ofId(UUID id);

    Optional<User> ofEmail(String email);

    Stream<User> allUsers();
}
