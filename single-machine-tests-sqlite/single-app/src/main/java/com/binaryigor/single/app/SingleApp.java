package com.binaryigor.single.app;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@SpringBootApplication
public class SingleApp {
    public static void main(String[] args) {
        SpringApplication.run(SingleApp.class, args);
    }

    @Bean
    InitializingBean sqliteInitializer(DataSource dataSource,
                                     @Value("${spring.datasource.init-statements}")
                                     String initStatements) {
        return () -> {
            try (var conn = dataSource.getConnection()) {
                for (var s : initStatements.split(";")) {
                    if (s.isBlank()) {
                        continue;
                    }
                    conn.createStatement()
                        .execute(s);
                }
            }
        };
    }
}
