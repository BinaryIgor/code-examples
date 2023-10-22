package com.binaryigor.apitests.support;

import com.binaryigor.apitests.api.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(value = "integration")
@ExtendWith(ClearTestDbExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = IntegrationTest.TestConfig.class)
public abstract class IntegrationTest {

    protected static final ExtendedPostgreSQLContainer POSTGRESQL_CONTAINER = ExtendedPostgreSQLContainer.instance();

    @Autowired
    protected TestHttpClient httpClient;

    protected void assertResponseStatus(TestHttpResponse response, HttpStatus expectedStatus) {
        Assertions.assertThat(response.statusCode()).isEqualTo(expectedStatus.value());
    }

    protected <T> void assertResponseBody(TestHttpResponse response,
                                          T expectedBody,
                                          Class<T> bodyClazz) {
        Assertions.assertThat(response.bodyAsJson(bodyClazz))
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
