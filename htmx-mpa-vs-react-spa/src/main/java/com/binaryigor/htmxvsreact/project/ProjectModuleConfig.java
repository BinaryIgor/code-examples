package com.binaryigor.htmxvsreact.project;

import com.binaryigor.htmxvsreact.project.exception.ProjectDoestNotExistException;
import com.binaryigor.htmxvsreact.project.exception.ProjectNameConflictException;
import com.binaryigor.htmxvsreact.project.exception.ProjectOwnerException;
import com.binaryigor.htmxvsreact.project.exception.ProjectValidationException;
import com.binaryigor.htmxvsreact.shared.Translations;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectModuleConfig {

    @Bean
    InitializingBean projectTranslationsInitializer() {
        return () -> {
            Translations.register(ProjectValidationException.class, e -> "Name can't be blank and needs to have between 3 and 100 characters");
            Translations.register(ProjectNameConflictException.class, e -> "Project of %s name already exists".formatted(e.name));
            Translations.register(ProjectOwnerException.class, e -> "Project doesn't belong to the current user");
            Translations.register(ProjectDoestNotExistException.class, e -> "Project of a given name doesn't exist");
        };
    }
}
