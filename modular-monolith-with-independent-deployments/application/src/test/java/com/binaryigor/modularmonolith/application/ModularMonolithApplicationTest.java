package com.binaryigor.modularmonolith.application;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(value = "integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = ModularMonolithApplicationTest.TestConfig.class)
public class ModularMonolithApplicationTest {

    @Test
    void shouldStartTheApplication() {
        System.out.println("Should start the application!");
    }

    @TestConfiguration
    static class TestConfig {

    }
}
