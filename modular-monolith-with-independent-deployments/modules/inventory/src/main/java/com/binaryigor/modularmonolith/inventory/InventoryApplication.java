package com.binaryigor.modularmonolith.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

@SpringBootApplication
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }

    @Bean
    @ConfigurationProperties("spring.datasource.inventory")
    public DataSourceProperties inventoryDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    public DataSource inventoryDataSource() {
        return inventoryDataSourceProperties()
                .initializeDataSourceBuilder()
                .build();
    }

    @Bean
    public JdbcTemplate inventoryJdbcTemplate(DataSource inventoryDataSource) {
        return new JdbcTemplate(inventoryDataSource);
    }

    @Bean
    public DataSourceInitializer inventoryDataSourceInitializer(DataSource inventoryDataSource) {
        var resourceDatabasePopulator = new ResourceDatabasePopulator(new ClassPathResource("/inventory_schema.sql"));

        var dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(inventoryDataSource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);

        return dataSourceInitializer;
    }

    @Bean
    PlatformTransactionManager inventoryTransactionManager(DataSource inventoryDataSource) {
        return new JdbcTransactionManager(inventoryDataSource);
    }

    @Bean
    public TransactionTemplate inventoryTransactionTemplate(PlatformTransactionManager inventoryTransactionManager) {
        return new TransactionTemplate(inventoryTransactionManager);
    }
}
