package com.binaryigor.complexity_alternative;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@ConfigurationPropertiesScan
@SpringBootApplication
public class ModernFrontendComplexityAlternativeApp {

    static void main(String[] args) {
        SpringApplication.run(ModernFrontendComplexityAlternativeApp.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
