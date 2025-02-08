package com.binaryigor.htmxvsreact.project;

import com.binaryigor.htmxvsreact.project.domain.ProjectRepository;
import com.binaryigor.htmxvsreact.project.domain.ProjectService;
import com.binaryigor.htmxvsreact.project.infra.SqlProjectRepository;
import com.binaryigor.htmxvsreact.project.domain.exception.ProjectDoestNotExistException;
import com.binaryigor.htmxvsreact.project.domain.exception.ProjectNameConflictException;
import com.binaryigor.htmxvsreact.project.domain.exception.ProjectNameException;
import com.binaryigor.htmxvsreact.project.domain.exception.ProjectOwnerException;
import com.binaryigor.htmxvsreact.shared.contracts.TaskClient;
import com.binaryigor.htmxvsreact.shared.html.Translations;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.time.Clock;

@Configuration
public class ProjectModuleConfig {

    @Bean
    InitializingBean projectTranslationsInitializer(Translations translations) {
        return () -> {
            translations.register(ProjectNameException.class, (l, e) -> "Name can't be blank and needs to have between 3 and 50 characters");
            translations.register(ProjectNameConflictException.class, (l, e) -> "Project of %s name already exists".formatted(e.name));
            translations.register(ProjectOwnerException.class, (l, e) -> "Project doesn't belong to the current user");
            translations.register(ProjectDoestNotExistException.class, (l, e) -> "Project doesn't exist");
        };
    }

    @Bean
    ProjectRepository projectRepository(JdbcClient jdbcClient, Clock clock) {
        return new SqlProjectRepository(jdbcClient, clock);
    }

    @Bean
    ProjectService projectService(ProjectRepository projectRepository, TaskClient taskClient) {
        return new ProjectService(projectRepository, taskClient);
    }
}
