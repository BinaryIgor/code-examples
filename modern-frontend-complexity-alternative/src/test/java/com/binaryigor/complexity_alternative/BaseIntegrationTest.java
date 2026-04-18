package com.binaryigor.complexity_alternative;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.client.RestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT)
public abstract class BaseIntegrationTest {

    @LocalServerPort
    protected int port;

    protected RestClient testRestClient;

    @BeforeEach
    void setup() {
        testRestClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }
}
