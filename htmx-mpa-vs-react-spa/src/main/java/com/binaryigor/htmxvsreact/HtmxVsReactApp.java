package com.binaryigor.htmxvsreact;

import com.binaryigor.htmxvsreact.project.ProjectRepository;
import com.binaryigor.htmxvsreact.project.ProjectService;
import com.binaryigor.htmxvsreact.project.db.SqlProjectRepository;
import com.binaryigor.htmxvsreact.shared.contracts.ProjectClient;
import com.binaryigor.htmxvsreact.shared.contracts.TaskClient;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import com.binaryigor.htmxvsreact.shared.html.HTMLConfig;
import com.binaryigor.htmxvsreact.shared.html.HTMLTemplates;
import com.binaryigor.htmxvsreact.shared.html.NoCacheTemplateFactory;
import com.binaryigor.htmxvsreact.shared.html.TemplateFactory;
import com.binaryigor.htmxvsreact.task.TaskRepository;
import com.binaryigor.htmxvsreact.task.TaskService;
import com.binaryigor.htmxvsreact.task.db.SqlTaskRepository;
import com.binaryigor.htmxvsreact.user.TheUserClient;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.resolver.FileSystemResolver;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.sqlite.SQLiteDataSource;

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
    ProjectRepository projectRepository(JdbcClient jdbcClient, Clock clock) {
        return new SqlProjectRepository(jdbcClient, clock);
    }

    @Bean
    ProjectService projectService(ProjectRepository projectRepository, TaskClient taskClient) {
        return new ProjectService(projectRepository, taskClient);
    }

    @Bean
    SqlTaskRepository taskRepository(JdbcClient jdbcClient) {
        return new SqlTaskRepository(jdbcClient);
    }

    @Bean
    TaskService taskService(TaskRepository taskRepository, ProjectClient projectClient) {
        return new TaskService(taskRepository, projectClient);
    }

    @Bean
    HikariDataSource dataSource(@Value("${spring.datasource.url}") String url,
                                SQLiteProperties sqliteProperties) {
        var dataSource = new SQLiteDataSource();
        dataSource.setBusyTimeout(sqliteProperties.busyTimeout);
        dataSource.setEnforceForeignKeys(true);
        dataSource.setUrl(url);
        dataSource.setCacheSize(sqliteProperties.cacheSize);
        dataSource.setJournalMode(sqliteProperties.journalMode);

        var config = new HikariConfig();
        config.setMinimumIdle(sqliteProperties.poolSize);
        config.setMaximumPoolSize(sqliteProperties.poolSize);
        config.setDataSource(dataSource);

        return new HikariDataSource(config);
    }

    @ConfigurationProperties("spring.datasource.sqlite")
    record SQLiteProperties(int poolSize, int busyTimeout, String journalMode, int cacheSize) {
    }
}
