package com.binaryigor.htmxvsreact.user;

import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import com.binaryigor.htmxvsreact.shared.contracts.UserView;

import java.util.UUID;

// TODO: real impl
public class TheUserClient implements UserClient {

    private static final UserView DEFAULT_USER = new UserView(UUID.fromString("c499e2f4-b9b6-40f1-a57f-4bffc779d0c9"), "Igor", "igor@email.com");

    @Override
    public UUID currentUserId() {
        return DEFAULT_USER.id();
    }

    @Override
    public UserView currentUser() {
        return DEFAULT_USER;
    }
}
