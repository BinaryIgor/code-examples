package com.binaryigor.restapitests.support;

import com.binaryigor.restapitests.api.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

@ActiveProfiles(value = "integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = IntegrationTest.TestConfig.class)
public abstract class IntegrationTest {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15");

    static {
        POSTGRESQL_CONTAINER.start();
        System.setProperty("DB_URL", POSTGRESQL_CONTAINER.getJdbcUrl());
        System.setProperty("DB_USERNAME", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("DB_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
    }

    @Autowired
    protected TestHttpClient testHttpClient;
    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @AfterEach
    void afterEach() {
        jdbcTemplate.execute("TRUNCATE client");
    }

    protected void assertResponseStatus(TestHttpResponse response, HttpStatus expectedStatus) {
        Assertions.assertThat(response.statusCode()).isEqualTo(expectedStatus.value());
    }

    protected <T> void assertResponseBody(TestHttpResponse response,
                                          T expectedBody) {
        Assertions.assertThat(response.bodyAsJson(expectedBody.getClass()))
                .isEqualTo(expectedBody);
    }


    protected void assertErrorResponse(TestHttpResponse response,
                                       HttpStatus expectedStatus,
                                       String expectedError) {
        assertResponseStatus(response, expectedStatus);
        Assertions.assertThat(response.bodyAsJson(ErrorResponse.class).error())
                .isEqualTo(expectedError);
    }

    protected <T extends Throwable> void assertErrorResponse(TestHttpResponse response,
                                                             HttpStatus expectedStatus,
                                                             Class<T> expectedError) {
        assertErrorResponse(response, expectedStatus, expectedError.getSimpleName());
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        ServerPortListener serverPortListener() {
            return new ServerPortListener();
        }

        @Bean
        TestHttpClient testHttpClient(ServerPortListener portListener, ObjectMapper objectMapper) {
            return new TestHttpClient(portListener::port, objectMapper);
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
