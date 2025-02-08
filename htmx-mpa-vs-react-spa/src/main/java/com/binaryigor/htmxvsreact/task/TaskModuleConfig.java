package com.binaryigor.htmxvsreact.task;

import com.binaryigor.htmxvsreact.shared.contracts.ProjectClient;
import com.binaryigor.htmxvsreact.shared.html.Translations;
import com.binaryigor.htmxvsreact.task.domain.TaskRepository;
import com.binaryigor.htmxvsreact.task.domain.TaskService;
import com.binaryigor.htmxvsreact.task.domain.TaskStatus;
import com.binaryigor.htmxvsreact.task.domain.exception.*;
import com.binaryigor.htmxvsreact.task.infra.SqlTaskRepository;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class TaskModuleConfig {

    @Bean
    InitializingBean taskTranslationsInitializer(Translations translations) {
        return () -> {
            translations.register(TaskNameValidationException.class, (l, e) -> "Name can't be blank and needs to have between 3 and 50 characters");
            translations.register(TaskProjectValidationException.class, (l, e) -> "Invalid task project. Allowed projects are: %s".formatted(String.join(", ", e.allowedProjects)));
            translations.register(TaskStatusValidationException.class, (l, e) -> {
                var validValues = Arrays.stream(TaskStatus.values()).map(Enum::name).collect(Collectors.joining(", "));
                if (e == null) {
                    return "Invalid status. Valid values are " + validValues;
                }
                return "%s is not a valid status. Valid values are: %s".formatted(e.status, validValues);
            });
            translations.register(TaskOwnerException.class, (l, e) -> "Task doesn't belong to the current user");
            translations.register(TaskProjectOwnerException.class, (l, e) -> "Task project doesn't belong to the current user");
        };
    }

    @Bean
    SqlTaskRepository taskRepository(JdbcClient jdbcClient) {
        return new SqlTaskRepository(jdbcClient);
    }

    @Bean
    TaskService taskService(TaskRepository taskRepository, ProjectClient projectClient) {
        return new TaskService(taskRepository, projectClient);
    }
}
