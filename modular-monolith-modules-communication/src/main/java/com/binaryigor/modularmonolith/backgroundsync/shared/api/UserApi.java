package com.binaryigor.modularmonolith.backgroundsync.shared.api;

import java.util.stream.Stream;

public interface UserApi {
    Stream<UserView> allUsers();
}
