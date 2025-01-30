package com.binaryigor.htmxvsreact.project;

import com.binaryigor.htmxvsreact.shared.Translations;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectModuleConfig {

    @Bean
    InitializingBean projectTranslationsInitializer() {
        return () -> {
            Translations.register(ProjectValidationException.class, (e) -> "Name can't be blank and needs to have between 3 and 100 characters");
        };
    }
}
