package com.binaryigor.modularpattern.project;

import com.binaryigor.modularpattern.project.domain.ProjectRepository;
import com.binaryigor.modularpattern.project.domain.ProjectService;
import com.binaryigor.modularpattern.project.domain.ProjectUserRepository;
import com.binaryigor.modularpattern.project.domain.ProjectUsersSync;
import com.binaryigor.modularpattern.project.infra.HttpUserClient;
import com.binaryigor.modularpattern.project.infra.SqlProjectRepository;
import com.binaryigor.modularpattern.project.infra.SqlProjectUserRepository;
import com.binaryigor.modularpattern.shared.contracts.UserClient;
import com.binaryigor.modularpattern.shared.db.SpringTransactions;
import com.binaryigor.modularpattern.shared.db.Transactions;
import com.binaryigor.modularpattern.shared.events.AppEvents;
import com.binaryigor.modularpattern.shared.events.AppEventsPublisher;
import com.binaryigor.modularpattern.shared.events.InMemoryAppEvents;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.net.http.HttpClient;
import java.time.Duration;

@SpringBootApplication
public class ProjectApplication {

    static final String PROJECT_DATA_SOURCE_BEAN = "projectDataSource";
    static final String PROJECT_TRANSACTIONS_BEAN = "projectTransactions";
    static final String PROJECT_JDBC_CLIENT_BEAN = "projectJdbcClient";

    public static void main(String[] args) {
        SpringApplication.run(ProjectApplication.class, args);
    }

    @Bean
    @ConfigurationProperties("spring.datasource.project")
    DataSourceProperties projectDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(PROJECT_DATA_SOURCE_BEAN)
    DataSource dataSource() {
        return projectDataSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }

    @Bean(PROJECT_JDBC_CLIENT_BEAN)
    JdbcClient jdbcClient(@Qualifier(PROJECT_DATA_SOURCE_BEAN) DataSource dataSource) {
        return JdbcClient.create(dataSource);
    }

    @Bean
    DataSourceInitializer projectDataSourceInitializer(@Qualifier(PROJECT_DATA_SOURCE_BEAN) DataSource dataSource) {
        var resourceDatabasePopulator = new ResourceDatabasePopulator(new ClassPathResource("/project_schema.sql"));

        var dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);

        return dataSourceInitializer;
    }

    @Bean(PROJECT_TRANSACTIONS_BEAN)
    Transactions transactions(@Qualifier(PROJECT_DATA_SOURCE_BEAN) DataSource dataSource) {
        var transactionManager = new JdbcTransactionManager(dataSource);
        return new SpringTransactions(new TransactionTemplate(transactionManager));
    }

    @Bean
    ProjectRepository projectRepository(@Qualifier(PROJECT_JDBC_CLIENT_BEAN) JdbcClient jdbcClient,
                                        @Qualifier(PROJECT_TRANSACTIONS_BEAN) Transactions transactions) {
        return new SqlProjectRepository(jdbcClient, transactions);
    }

    @Bean
    ProjectUserRepository projectUserRepository(@Qualifier(PROJECT_JDBC_CLIENT_BEAN) JdbcClient jdbcClient) {
        return new SqlProjectUserRepository(jdbcClient);
    }

    @Bean
    ProjectService projectService(ProjectRepository projectRepository,
                                  ProjectUserRepository projectUserRepository) {
        return new ProjectService(projectRepository, projectUserRepository);
    }

    @Profile("!monolith")
    @Bean
    InMemoryAppEvents appEvents() {
        return new InMemoryAppEvents();
    }

    @Profile("!monolith")
    @Bean
    AppEventsPublisher appEventsPublisher(InMemoryAppEvents appEvents) {
        return appEvents.publisher();
    }

    @Bean
    ProjectUsersSync projectUsersSync(ProjectUserRepository projectUserRepository,
                                      UserClient userClient,
                                      AppEvents appEvents) {
        return new ProjectUsersSync(projectUserRepository, userClient, appEvents);
    }

    @Profile("!monolith")
    @Bean
    HttpUserClient httpUserClient(@Value("${http-user-client.host}") String host,
                                  ObjectMapper objectMapper) {
        var connectTimeout = Duration.ofSeconds(1);
        var readTimeout = Duration.ofSeconds(10);

        var httpClient = HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .build();

        return new HttpUserClient(httpClient, host, readTimeout, objectMapper);
    }
}
