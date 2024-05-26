package com.binaryigor.modularpattern.user;

import com.binaryigor.modularpattern.shared.db.SpringTransactions;
import com.binaryigor.modularpattern.shared.db.Transactions;
import com.binaryigor.modularpattern.shared.events.AppEvents;
import com.binaryigor.modularpattern.shared.events.AppEventsPublisher;
import com.binaryigor.modularpattern.shared.events.InMemoryAppEvents;
import com.binaryigor.modularpattern.shared.leader.SingleInstanceLeaderAwareness;
import com.binaryigor.modularpattern.shared.outbox.OutboxProcessor;
import com.binaryigor.modularpattern.shared.outbox.OutboxRepository;
import com.binaryigor.modularpattern.shared.outbox.SqlOutboxRepository;
import com.binaryigor.modularpattern.user.domain.UserRepository;
import com.binaryigor.modularpattern.user.domain.UserService;
import com.binaryigor.modularpattern.user.infra.HttpUserChangedPublisher;
import com.binaryigor.modularpattern.user.infra.ScheduledOutboxProcessor;
import com.binaryigor.modularpattern.user.infra.SqlUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.net.http.HttpClient;
import java.time.Duration;

@SpringBootApplication
@EnableScheduling
public class UserApplication {

    static final String USER_DATA_SOURCE_BEAN = "userDataSource";
    static final String USER_TRANSACTIONS_BEAN = "userTransactions";
    static final String USER_JDBC_CLIENT_BEAN = "userJdbcClient";
    static final String USER_OUTBOX_REPOSITORY_BEAN = "userOutboxRepository";

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @Bean
    @ConfigurationProperties("spring.datasource.user")
    DataSourceProperties userDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(USER_DATA_SOURCE_BEAN)
    DataSource dataSource() {
        return userDataSourceProperties()
            .initializeDataSourceBuilder()
            .build();
    }

    @Bean(USER_JDBC_CLIENT_BEAN)
    JdbcClient jdbcClient(@Qualifier(USER_DATA_SOURCE_BEAN) DataSource dataSource) {
        return JdbcClient.create(dataSource);
    }

    @Bean
    DataSourceInitializer userDataSourceInitializer(@Qualifier(USER_DATA_SOURCE_BEAN) DataSource dataSource) {
        var resourceDatabasePopulator = new ResourceDatabasePopulator(new ClassPathResource("/user_schema.sql"));

        var dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);

        return dataSourceInitializer;
    }

    @Bean(USER_TRANSACTIONS_BEAN)
    Transactions transactions(@Qualifier(USER_DATA_SOURCE_BEAN) DataSource dataSource) {
        var transactionManager = new JdbcTransactionManager(dataSource);
        return new SpringTransactions(new TransactionTemplate(transactionManager));
    }

    @Bean
    UserRepository userRepository(@Qualifier(USER_JDBC_CLIENT_BEAN) JdbcClient jdbcClient) {
        return new SqlUserRepository(jdbcClient);
    }

    @Bean(USER_OUTBOX_REPOSITORY_BEAN)
    OutboxRepository outboxRepository(@Qualifier(USER_JDBC_CLIENT_BEAN) JdbcClient jdbcClient,
                                      ObjectMapper objectMapper) {
        return new SqlOutboxRepository(jdbcClient, objectMapper);
    }

    @Bean
    UserService userService(UserRepository userRepository,
                            @Qualifier(USER_OUTBOX_REPOSITORY_BEAN) OutboxRepository outboxRepository,
                            @Qualifier(USER_TRANSACTIONS_BEAN) Transactions transactions) {
        return new UserService(userRepository, outboxRepository, transactions);
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

    @Profile("!integration")
    @Bean
    ScheduledOutboxProcessor userScheduledOutboxProcessor(OutboxRepository userOutboxRepository,
                                                          AppEventsPublisher appEventsPublisher) {
        var processor = new OutboxProcessor(userOutboxRepository, appEventsPublisher, 100);
        return new ScheduledOutboxProcessor(processor, new SingleInstanceLeaderAwareness());
    }

    @ConditionalOnProperty(name = "http-user-changed-publisher.host")
    @Bean
    HttpUserChangedPublisher httpUserChangedPublisher(@Value("${http-user-changed-publisher.host}") String host,
                                                      ObjectMapper objectMapper,
                                                      AppEvents appEvents) {
        // TODO: maybe properties
        var connectTimeout = Duration.ofSeconds(1);
        var publishTimeout = Duration.ofSeconds(1);

        var httpClient = HttpClient.newBuilder()
            .connectTimeout(connectTimeout)
            .build();

        return new HttpUserChangedPublisher(httpClient, host, publishTimeout, objectMapper, appEvents);
    }
}
