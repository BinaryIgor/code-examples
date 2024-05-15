package com.binaryigor.modularmonolith.backgroundsync.user;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface UserRepository {

    void save(User user);

    Optional<User> ofId(UUID id);

    Stream<User> allUsers();
}
