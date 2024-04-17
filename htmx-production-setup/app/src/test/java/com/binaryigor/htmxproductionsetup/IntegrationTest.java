package com.binaryigor.htmxproductionsetup;

import com.binaryigor.htmxproductionsetup.user.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.TimeZone;

@ActiveProfiles(value = {"integration"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = IntegrationTest.TestConfig.class,
        properties = {"ENV=integration"})
public abstract class IntegrationTest {

    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRESQL_CONTAINER.start();
        System.setProperty("DB_URL", POSTGRESQL_CONTAINER.getJdbcUrl());
        System.setProperty("DB_USER", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("DB_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
    }

    static {
        //Prevent strange behavior during daylight saving time
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    @Autowired
    protected TestRestTemplate restTemplate;
    @Autowired
    protected UserTestFixture userTestFixture;
    @Autowired
    private JdbcClient jdbcClient;

    protected void afterSetup() {
        jdbcClient.sql("""
                TRUNCATE TABLE "user"."user";
                TRUNCATE TABLE "day"."day";
                """);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        UserTestFixture userTestFixture(UserRepository userRepository,
                                        TestRestTemplate restTemplate) {
            return new UserTestFixture(userRepository, restTemplate);
        }

    }

    static class ServerPortListener {
        private int port;

        public int port() {
            return port;
        }

        @EventListener
        public void onApplicationEvent(ServletWebServerInitializedEvent event) {
            port = event.getWebServer().getPort();
        }
    }
}
