package com.binaryigor.modularpattern.monolith;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.binaryigor.modularpattern"})
public class ModularMonolithApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModularMonolithApplication.class, args);
    }
}
