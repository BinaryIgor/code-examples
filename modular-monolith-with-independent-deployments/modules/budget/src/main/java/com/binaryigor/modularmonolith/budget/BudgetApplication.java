package com.binaryigor.modularmonolith.budget;

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

@SpringBootApplication
public class BudgetApplication {

    public static void main(String[] args) {
        SpringApplication.run(BudgetApplication.class, args);
    }

    @Bean
    @ConfigurationProperties("spring.datasource.budget")
    public DataSourceProperties budgetDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource budgetDataSource() {
        return budgetDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public JdbcTemplate budgetJdbcTemplate(DataSource budgetDataSource) {
        return new JdbcTemplate(budgetDataSource);
    }

    @Bean
    public DataSourceInitializer budgetDataSourceInitializer(DataSource budgetDataSource) {
        var resourceDatabasePopulator = new ResourceDatabasePopulator(new ClassPathResource("/budget_schema.sql"));

        var dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(budgetDataSource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);

        return dataSourceInitializer;
    }
}
