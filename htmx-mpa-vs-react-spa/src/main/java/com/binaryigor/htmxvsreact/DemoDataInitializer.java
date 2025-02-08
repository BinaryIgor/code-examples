package com.binaryigor.htmxvsreact;

import com.binaryigor.htmxvsreact.project.domain.Project;
import com.binaryigor.htmxvsreact.project.domain.ProjectRepository;
import com.binaryigor.htmxvsreact.shared.AppLanguage;
import com.binaryigor.htmxvsreact.user.domain.User;
import com.binaryigor.htmxvsreact.user.domain.UserRepository;
import com.binaryigor.htmxvsreact.user.domain.PasswordHasher;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DemoDataInitializer {

    private static final UUID USER1_ID = UUID.fromString("988452d8-47f8-434d-b213-e04a34b727b1");
    private static final UUID USER2_ID = UUID.fromString("4ab93360-ceee-4611-815b-e1b265a910d9");
    private static final UUID PROJECT1_ID = UUID.fromString("e7764dba-cb29-405d-bb3d-6e6d47ea24d1");
    private static final UUID PROJECT2_ID = UUID.fromString("fc3ff8d3-814d-454b-b33a-e026a275b4c2");
    private static final Logger logger = LoggerFactory.getLogger(DemoDataInitializer.class);
    private final UserRepository userRepository;
    private final ProjectRepository projectRepository;
    private final PasswordHasher passwordHasher;

    public DemoDataInitializer(UserRepository userRepository,
                               ProjectRepository projectRepository,
                               PasswordHasher passwordHasher) {
        this.userRepository = userRepository;
        this.projectRepository = projectRepository;
        this.passwordHasher = passwordHasher;
    }

    @PostConstruct
    public void init() {
        logger.info("Initializing db with demo data...");

        var user1 = new User(USER1_ID, "igor@gmail.com", "Igor",
            passwordHasher.hash("ComplexPassword12"), AppLanguage.EN);
        var user2 = new User(USER2_ID, "other@gmail.com", "Other",
            passwordHasher.hash("ComplexOtherPassword12"), AppLanguage.EN);

        userRepository.save(user1);
        userRepository.save(user2);

        var project1 = new Project(PROJECT1_ID, "Project 1", user1.id());
        var project2 = new Project(PROJECT2_ID, "Project 2", user1.id());

        projectRepository.save(project1);
        projectRepository.save(project2);

        logger.info("Db initialized!");
    }
}
