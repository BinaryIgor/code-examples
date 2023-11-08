package com.binaryigor.modularmonolith.campaign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;
import java.time.Clock;

@SpringBootApplication
public class CampaignApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampaignApplication.class, args);
    }

    @Bean
    public Clock campaignClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConfigurationProperties("spring.datasource.campaign")
    public DataSourceProperties campaignDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource campaignDataSource() {
        return campaignDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public JdbcTemplate campaignJdbcTemplate(DataSource campaignDataSource) {
        return new JdbcTemplate(campaignDataSource);
    }

    @Bean
    public DataSourceInitializer campaignDataSourceInitializer(DataSource campaignDataSource) {
        var resourceDatabasePopulator = new ResourceDatabasePopulator(new ClassPathResource("/campaign_schema.sql"));

        var dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(campaignDataSource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);

        return dataSourceInitializer;
    }
}
