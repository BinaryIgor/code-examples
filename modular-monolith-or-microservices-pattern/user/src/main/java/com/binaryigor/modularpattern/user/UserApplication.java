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
import com.binaryigor.modularpattern.user.infra.ScheduledOutboxProcessor;
import com.binaryigor.modularpattern.user.infra.SqlUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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

@SpringBootApplication
@EnableScheduling
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

    @Bean
    @ConfigurationProperties("spring.datasource.user")
    DataSourceProperties userDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    DataSource userDataSource() {
        return userDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    JdbcClient userJdbcClient(DataSource userDataSource) {
        return JdbcClient.create(userDataSource);
    }

    @Bean
    DataSourceInitializer userDataSourceInitializer(DataSource userDataSource) {
        var resourceDatabasePopulator = new ResourceDatabasePopulator(new ClassPathResource("/user_schema.sql"));

        var dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(userDataSource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);

        return dataSourceInitializer;
    }

    @Bean
    Transactions userTransactions(DataSource userDataSource) {
        var transactionManager = new JdbcTransactionManager(userDataSource);
        return new SpringTransactions(new TransactionTemplate(transactionManager));
    }

    @Bean
    UserRepository userRepository(JdbcClient userJdbcClient) {
        return new SqlUserRepository(userJdbcClient);
    }

    @Bean
    OutboxRepository userOutboxRepository(JdbcClient userJdbcClient,
                                          ObjectMapper objectMapper) {
        return new SqlOutboxRepository(userJdbcClient, objectMapper);
    }

    @Bean
    UserService userService(UserRepository userRepository,
                            OutboxRepository userOutboxRepository,
                            Transactions userTransactions) {
        return new UserService(userRepository, userOutboxRepository, userTransactions);
    }

    @ConditionalOnMissingBean(AppEvents.class)
    @Bean
    InMemoryAppEvents appEvents() {
        return new InMemoryAppEvents();
    }

    @ConditionalOnMissingBean(AppEventsPublisher.class)
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
}
