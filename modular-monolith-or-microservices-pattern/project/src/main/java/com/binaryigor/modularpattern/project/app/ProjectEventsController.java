package com.binaryigor.modularpattern.project.app;

import com.binaryigor.modularpattern.project.domain.ProjectUsersSync;
import com.binaryigor.modularpattern.shared.contracts.UserChangedEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@Profile("!monolith")
@RestController
@RequestMapping("/events")
public class ProjectEventsController {

    private final ProjectUsersSync projectUsersSync;

    public ProjectEventsController(ProjectUsersSync projectUsersSync) {
        this.projectUsersSync = projectUsersSync;
    }

    @PostMapping("{type}")
    void onUserChanged(@PathVariable("type") String type,
                       @RequestBody UserChangedEvent event) {
        System.out.println("Event: " + event);
        if (type.equals(UserChangedEvent.class.getSimpleName())) {
            projectUsersSync.onUserChangedEvent(event);
        }
    }
}
