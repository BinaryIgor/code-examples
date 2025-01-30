package com.binaryigor.htmxvsreact.user;

import com.binaryigor.htmxvsreact.shared.UserClient;

import java.util.UUID;

// TODO: real impl
public class TheUserClient implements UserClient {

    private static final UUID DEFAULT_ID = UUID.fromString("c499e2f4-b9b6-40f1-a57f-4bffc779d0c9");

    @Override
    public UUID currentUserId() {
        return DEFAULT_ID;
    }
}
