package com.binaryigor.modularpattern.project.app;

import com.binaryigor.modularpattern.project.domain.ProjectUsersSync;
import com.binaryigor.modularpattern.shared.contracts.UserChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.*;

@ProjectControllerTag
@Profile("!monolith")
@RestController
@RequestMapping("/events")
public class ProjectEventsController {

    private final Logger logger = LoggerFactory.getLogger(ProjectEventsController.class);

    private final ProjectUsersSync projectUsersSync;

    public ProjectEventsController(ProjectUsersSync projectUsersSync) {
        this.projectUsersSync = projectUsersSync;
    }

    @PostMapping("{type}")
    void onUserChanged(@PathVariable("type") String type,
                       @RequestBody UserChangedEvent event) {
        logger.info("Event: {}, {}", type, event);
        if (type.equals(UserChangedEvent.class.getSimpleName())) {
            projectUsersSync.onUserChangedEvent(event);
        }
    }
}
