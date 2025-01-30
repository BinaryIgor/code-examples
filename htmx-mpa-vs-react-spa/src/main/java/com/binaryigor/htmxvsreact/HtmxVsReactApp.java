package com.binaryigor.htmxvsreact;

import com.binaryigor.htmxvsreact.html.HTMLConfig;
import com.binaryigor.htmxvsreact.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.html.NoCacheTemplateFactory;
import com.binaryigor.htmxvsreact.html.TemplateFactory;
import com.binaryigor.htmxvsreact.project.InMemoryProjectRepository;
import com.binaryigor.htmxvsreact.project.ProjectRepository;
import com.binaryigor.htmxvsreact.project.ProjectValidationException;
import com.binaryigor.htmxvsreact.shared.Translations;
import com.binaryigor.htmxvsreact.shared.UserClient;
import com.binaryigor.htmxvsreact.user.TheUserClient;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.resolver.FileSystemResolver;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@ConfigurationPropertiesScan
@SpringBootApplication
public class HtmxVsReactApp {
    public static void main(String[] args) {
        SpringApplication.run(HtmxVsReactApp.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    TemplateFactory templateFactory() {
        return new NoCacheTemplateFactory(() -> new DefaultMustacheFactory(new FileSystemResolver()));
    }

    @Bean
    HTMLTemplates htmlTemplates(TemplateFactory templateFactory, HTMLConfig htmlConfig) {
        return new HTMLTemplates(templateFactory, htmlConfig);
    }

    @Bean
    UserClient userClient() {
        return new TheUserClient();
    }

    @Bean
    ProjectRepository projectRepository() {
        return new InMemoryProjectRepository();
    }
}
