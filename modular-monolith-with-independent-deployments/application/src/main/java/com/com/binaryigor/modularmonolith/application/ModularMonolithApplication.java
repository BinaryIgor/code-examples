package com.binaryigor.modularmonolith.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

@SpringBootApplication(scanBasePackages = {"com.binaryigor.modularmonolith"})
public class ModularMonolithApplication {
    public static void main(String[] args) {
//        var campaignModule = new CampaignModule();
//        var budgetModule = new BudgetModule();
//
//        campaignModule.start();
//        budgetModule.start();
        SpringApplication.run(ModularMonolithApplication.class, args);
    }

    @Bean
    public Clock clock() {
        return Clock.fixed(Instant.MIN, ZoneId.of("UTC"));
    }
}
