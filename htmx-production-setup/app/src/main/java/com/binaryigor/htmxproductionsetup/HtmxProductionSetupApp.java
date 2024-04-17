package com.binaryigor.htmxproductionsetup;

import com.binaryigor.htmxproductionsetup.auth.AuthConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.time.Clock;

@SpringBootApplication(scanBasePackages = "com.binaryigor.htmxproductionsetup")
@EnableConfigurationProperties(AuthConfig.class)
public class HtmxProductionSetupApp {
    public static void main(String[] args) {
        SpringApplication.run(HtmxProductionSetupApp.class, args);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
