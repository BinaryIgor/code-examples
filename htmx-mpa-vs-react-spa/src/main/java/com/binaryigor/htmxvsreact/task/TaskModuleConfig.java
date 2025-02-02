package com.binaryigor.htmxvsreact.task;

import com.binaryigor.htmxvsreact.shared.Translations;
import com.binaryigor.htmxvsreact.task.exception.TaskNameValidationException;
import com.binaryigor.htmxvsreact.task.exception.TaskProjectValidationException;
import com.binaryigor.htmxvsreact.task.exception.TaskStatusValidationException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.stream.Collectors;

@Configuration
public class TaskModuleConfig {

    @Bean
    InitializingBean taskTranslationsInitializer() {
        return () -> {
            Translations.register(TaskNameValidationException.class, e -> "Name can't be blank and needs to have between 3 and 100 characters");
            Translations.register(TaskProjectValidationException.class, e -> "Project can't be blank and needs to have between 3 and 100 characters");
            Translations.register(TaskStatusValidationException.class, e -> {
                var validValues = Arrays.stream(TaskStatus.values()).map(Enum::name).collect(Collectors.joining(", "));
                if (e == null) {
                    return "Invalid status. Valid values are " + validValues;
                }
                return "%s is not a valid status. Valid values are: %s".formatted(e.status, validValues);
            });
        };
    }
}
