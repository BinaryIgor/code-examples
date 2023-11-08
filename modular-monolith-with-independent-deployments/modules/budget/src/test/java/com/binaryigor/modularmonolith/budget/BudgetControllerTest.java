package com.binaryigor.modularmonolith.budget;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles(value = "integration")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = BudgetControllerTest.TestConfig.class)
public class BudgetControllerTest {

    @Test
    void shouldStartTheApplication() {
        System.out.println("Should start the application!");
    }

    @TestConfiguration
    static class TestConfig {

    }
}
