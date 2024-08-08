package com.binaryigor.simplewebanalytics;

import com.binaryigor.simplewebanalytics.core.AnalyticsEvent;
import com.binaryigor.simplewebanalytics.db.SqlAnalyticsEventRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles(value = {"integration"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = IntegrationTest.TestConfig.class)
public abstract class IntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:16");

    static {
        POSTGRESQL_CONTAINER.start();
        System.setProperty("DB_URL", POSTGRESQL_CONTAINER.getJdbcUrl());
        System.setProperty("DB_USERNAME", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("DB_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
    }

    @Autowired
    protected TestClock testClock;
    @Autowired
    protected TestRestTemplate restTemplate;
    @Autowired
    protected JdbcClient jdbcClient;
    @Autowired
    private SqlAnalyticsEventRepository analyticsEventRepository;

    @BeforeEach
    void setup() {
        jdbcClient.sql("""
            CREATE TABLE IF NOT EXISTS analytics_event (
                timestamp TIMESTAMP NOT NULL,
                ip TEXT NOT NULL,
                device_id UUID NOT NULL,
                user_id UUID,
                url TEXT NOT NULL,
                browser TEXT NOT NULL,
                platform TEXT NOT NULL,
                device TEXT NOT NULL,
                type TEXT NOT NULL,
                data JSONB
            );
            TRUNCATE analytics_event;
            """).update();
    }

    protected void delay(int millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void assertEventsWereAdded(AnalyticsEvent... events) {
        Assertions.assertThat(analyticsEventRepository.all())
            .containsExactly(events);
    }

    protected void assertNoEventsWereAdded() {
        Assertions.assertThat(analyticsEventRepository.all()).isEmpty();
    }

    @TestConfiguration
    static class TestConfig {

        @Primary
        @Bean
        TestClock testClock() {
            return new TestClock();
        }
    }
}
