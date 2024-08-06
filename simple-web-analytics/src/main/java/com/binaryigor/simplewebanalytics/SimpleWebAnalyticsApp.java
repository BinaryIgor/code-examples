package com.binaryigor.simplewebanalytics;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication
public class SimpleWebAnalyticsApp {
    public static void main(String[] args) {
        SpringApplication.run(SimpleWebAnalyticsApp.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    UserAuth userAuth() {
        return new HeaderUserAuth();
    }
}
