package com.binaryigor.modularpattern.shared.contracts;

import java.util.stream.Stream;

public interface UserClient {
    Stream<UserView> allUsers();
}
