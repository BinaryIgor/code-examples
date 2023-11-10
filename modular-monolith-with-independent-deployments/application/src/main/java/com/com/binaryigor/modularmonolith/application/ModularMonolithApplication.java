package com.binaryigor.modularmonolith.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.binaryigor.modularmonolith"})
public class ModularMonolithApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModularMonolithApplication.class, args);
    }
}
