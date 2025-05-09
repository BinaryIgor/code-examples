package com.binaryigor.htmxvsreact;

import com.binaryigor.htmxvsreact.project.domain.ProjectRepository;
import com.binaryigor.htmxvsreact.shared.contracts.UserClient;
import com.binaryigor.htmxvsreact.shared.error.WebExceptionHandler;
import com.binaryigor.htmxvsreact.shared.html.*;
import com.binaryigor.htmxvsreact.user.domain.PasswordHasher;
import com.binaryigor.htmxvsreact.user.domain.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.sqlite.SQLiteDataSource;

import java.time.Clock;

@ConfigurationPropertiesScan
@SpringBootApplication
public class HtmxVsReactApp implements WebMvcConfigurer {

    private final boolean corsEnabled;

    public HtmxVsReactApp(@Value("${cors.enabled}") boolean corsEnabled) {
        this.corsEnabled = corsEnabled;
    }

    public static void main(String[] args) {
        SpringApplication.run(HtmxVsReactApp.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Profile("dev")
    @Bean
    TemplateFactory devTemplateFactory() {
        return new NoCacheTemplateFactory(() -> new DefaultMustacheFactory(new FileSystemResolver()));
    }

    @Profile("!dev")
    @Bean
    TemplateFactory templateFactory() {
        var mustacheFactory = new DefaultMustacheFactory();
        return mustacheFactory::compile;
    }

    @Bean
    Translations translations(UserClient userClient) {
        return new Translations(userClient::currentLanguage);
    }

    @Bean
    HTMLTemplates htmlTemplates(TemplateFactory templateFactory, HTMLConfig htmlConfig, Translations translations) {
        return new HTMLTemplates(templateFactory, htmlConfig, translations);
    }

    @Bean
    WebExceptionHandler webExceptionHandler(HTMLTemplates htmlTemplates, Translations translations, ObjectMapper objectMapper) {
        return new WebExceptionHandler(htmlTemplates, translations, objectMapper);
    }

    @Bean
    HikariDataSource dataSource(@Value("${spring.datasource.url}") String url, SQLiteProperties sqliteProperties) {
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

    @Bean
    DemoDataInitializer demoDataInitializer(UserRepository userRepository, ProjectRepository projectRepository, PasswordHasher passwordHasher) {
        return new DemoDataInitializer(userRepository, projectRepository, passwordHasher);
    }

    // Allows everything and everyone, if enabled - change according to your needs
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (corsEnabled) {
            registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowCredentials(true);
        }
    }

    @ConfigurationProperties("spring.datasource.sqlite")
    record SQLiteProperties(int poolSize, int busyTimeout, String journalMode, int cacheSize) {
    }
}
