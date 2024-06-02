package com.binaryigor.modularpattern.project.domain;

import com.binaryigor.modularpattern.shared.Streams;
import com.binaryigor.modularpattern.shared.contracts.UserChangedEvent;
import com.binaryigor.modularpattern.shared.contracts.UserClient;
import com.binaryigor.modularpattern.shared.events.AppEvents;

import java.util.List;
import java.util.stream.Stream;

public class ProjectUsersSync {

    private final ProjectUserRepository projectUserRepository;
    private final UserClient userClient;

    public ProjectUsersSync(ProjectUserRepository projectUserRepository,
                            UserClient userClient,
                            AppEvents appEvents) {
        this.projectUserRepository = projectUserRepository;
        this.userClient = userClient;

        appEvents.subscribe(UserChangedEvent.class, this::onUserChangedEvent);
    }

    public void onUserChangedEvent(UserChangedEvent event) {
        var eventUser = event.user();
        var currentUser = projectUserRepository.ofId(eventUser.id());
        if (currentUser.isEmpty() || eventUser.version() >= currentUser.get().version()) {
            projectUserRepository.save(List.of(ProjectUser.fromUserView(event.user())));
        }
    }

    public void syncAll(int chunkSize) {
        try (var usersStream = Streams.chunked(userClient.allUsers(), chunkSize)) {
            usersStream.forEach(users -> {
                var projectUsers = users.stream().map(ProjectUser::fromUserView).toList();
                projectUserRepository.save(projectUsers);
            });
        }
    }

    public Stream<ProjectUser> allAvailable() {
        return projectUserRepository.allUsers();
    }
}
