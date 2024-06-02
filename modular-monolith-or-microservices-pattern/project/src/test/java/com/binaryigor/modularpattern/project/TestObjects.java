package com.binaryigor.modularpattern.project;

import com.binaryigor.modularpattern.project.domain.Project;
import com.binaryigor.modularpattern.project.domain.ProjectUser;
import com.binaryigor.modularpattern.shared.contracts.UserView;

import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TestObjects {

    private static final Random RANDOM = new Random();

    public static UserView randomUserView(UUID id, long version) {
        return new UserView(id, id + "-email@email.com", id + "-name", version);
    }

    public static UserView randomUserView(long version) {
        return randomUserView(UUID.randomUUID(), version);
    }

    public static UserView randomUserView() {
        return randomUserView(1 + RANDOM.nextLong(100));
    }

    public static ProjectUser randomProjectUser() {
        return ProjectUser.fromUserView(randomUserView());
    }

    public static Project randomProject(List<UUID> userIds) {
        var id = UUID.randomUUID();
        return new Project(id, id + "-name", null, userIds);
    }
}
