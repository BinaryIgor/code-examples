package com.binaryigor.htmxvsreact.shared.contracts;

import java.util.UUID;

public interface UserClient {

    UUID currentUserId();

    UserView currentUser();
}
