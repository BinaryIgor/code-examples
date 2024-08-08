package com.binaryigor.simplewebanalytics;

import com.binaryigor.simplewebanalytics.core.AnalyticsEventHandler;
import com.binaryigor.simplewebanalytics.core.AnalyticsEventRepository;
import com.binaryigor.simplewebanalytics.db.SqlAnalyticsEventRepository;
import com.binaryigor.simplewebanalytics.web.HeaderUserAuth;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Clock;

@Configuration
public class SimpleWebAnalyticsConfiguration implements WebMvcConfigurer {

    // Allows everything and everyone - change according to your needs
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOriginPatterns("*")
            .allowedHeaders("*")
            .allowedMethods("*")
            .allowCredentials(true);
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    UserAuth userAuth() {
        return new HeaderUserAuth();
    }

    @Bean
    AnalyticsEventRepository analyticsEventRepository(JdbcClient jdbcClient, ObjectMapper objectMapper) {
        return new SqlAnalyticsEventRepository(jdbcClient, objectMapper);
    }

    @Bean(destroyMethod = "shutdown")
    AnalyticsEventHandler analyticsEventHandler(AnalyticsEventRepository analyticsEventRepository,
                                                ObjectMapper objectMapper,
                                                @Value("${analytics-events.batch-size}")
                                                int batchSize,
                                                @Value("${analytics-events.check-batch-delay}")
                                                int checkBatchDelay) {
        return new AnalyticsEventHandler(analyticsEventRepository, objectMapper, batchSize, checkBatchDelay);
    }
}
