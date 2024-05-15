package com.binaryigor.modularmonolith.backgroundsync.project;

import com.binaryigor.modularmonolith.backgroundsync.shared.api.UserApi;
import com.binaryigor.modularmonolith.backgroundsync.shared.api.UserChangedEvent;
import com.binaryigor.modularmonolith.backgroundsync.shared.events.ApplicationEvents;

import java.util.LinkedList;

public class ProjectUsersSynchronization {

    private static final int WRITE_BATCH = 100;
    private final ProjectUserRepository projectUserRepository;
    private final UserApi userApi;

    public ProjectUsersSynchronization(ProjectUserRepository projectUserRepository,
                                       UserApi userApi,
                                       ApplicationEvents events) {
        this.projectUserRepository = projectUserRepository;
        this.userApi = userApi;

        events.subscribe(UserChangedEvent.class, e -> {
            System.out.println("Saving project user..." + e);
            projectUserRepository.save(new ProjectUser(e.id(), e.name(), e.email()));
        });
    }

    public void synchronizeAll() {
        var usersToSave = new LinkedList<ProjectUser>();

        userApi.allUsers()
                .forEach(u -> {
                    System.out.println("Saving..." + u);
                    usersToSave.add(new ProjectUser(u.id(), u.name(), u.email()));
                    if (usersToSave.size() >= WRITE_BATCH) {
                        projectUserRepository.saveAll(usersToSave);
                        usersToSave.clear();
                    }
                });

        if (!usersToSave.isEmpty()) {
            projectUserRepository.saveAll(usersToSave);
        }
    }
}
