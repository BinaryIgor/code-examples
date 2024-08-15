package com.binaryigor.simplewebanalytics;

import com.binaryigor.simplewebanalytics.generator.EventsGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class SimpleWebAnalyticsApp implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(SimpleWebAnalyticsApp.class);

    @Autowired
    private ConfigurableApplicationContext context;
    @Autowired
    private Environment environment;
    @Autowired(required = false)
    private EventsGenerator eventsGenerator;

    public static void main(String[] args) {
        SpringApplication.run(SimpleWebAnalyticsApp.class, args);
    }

    @Override
    public void run(String... args) {
        if (!environment.matchesProfiles("events-generator")) {
            return;
        }
        try {
            logger.info("Events generator profile is active, generating events");
            var eventsSize = Integer.parseInt(environment.getProperty("EVENTS_SIZE", "10000"));
            var eventsConcurrency = Integer.parseInt(environment.getProperty("EVENTS_CONCURRENCY", "250"));
            eventsGenerator.generate(eventsSize, eventsConcurrency);
        } catch (Exception e) {
            logger.error("Failure while generating events: ", e);
        } finally {
            context.close();
        }
    }
}
