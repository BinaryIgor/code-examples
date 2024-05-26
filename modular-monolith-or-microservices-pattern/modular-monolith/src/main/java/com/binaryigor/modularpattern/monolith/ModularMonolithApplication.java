package com.binaryigor.modularpattern.monolith;

import com.binaryigor.modularpattern.shared.events.AppEventsPublisher;
import com.binaryigor.modularpattern.shared.events.InMemoryAppEvents;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@SpringBootApplication(scanBasePackages = {"com.binaryigor.modularpattern"})
public class ModularMonolithApplication {
    public static void main(String[] args) {
        SpringApplication.run(ModularMonolithApplication.class, args);
    }

    @Bean
    InMemoryAppEvents appEvents() {
        return new InMemoryAppEvents();
    }

    @Bean
    AppEventsPublisher appEventsPublisher(InMemoryAppEvents appEvents) {
        return appEvents.publisher();
    }
}
