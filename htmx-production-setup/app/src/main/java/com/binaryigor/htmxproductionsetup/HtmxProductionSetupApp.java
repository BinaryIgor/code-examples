package com.binaryigor.htmxproductionsetup;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

//TODO: scan less of a classpath
@SpringBootApplication
public class HtmxProductionSetupApp {
    public static void main(String[] args) {
        SpringApplication.run(HtmxProductionSetupApp.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
