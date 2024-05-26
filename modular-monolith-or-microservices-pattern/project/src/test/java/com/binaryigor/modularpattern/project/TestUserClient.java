package com.binaryigor.modularpattern.project;

import com.binaryigor.modularpattern.shared.contracts.UserClient;
import com.binaryigor.modularpattern.shared.contracts.UserView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class TestUserClient implements UserClient {

    private final List<UserView> users = new ArrayList<>();

    public void addUsers(List<UserView> users) {
        this.users.addAll(users);
    }

    public void clear() {
        users.clear();
    }

    @Override
    public Stream<UserView> allUsers() {
        return users.stream();
    }
}
