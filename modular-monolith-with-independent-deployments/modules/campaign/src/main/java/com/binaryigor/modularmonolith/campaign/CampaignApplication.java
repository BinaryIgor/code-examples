package com.binaryigor.modularmonolith.campaign;

import com.binaryigor.modularmonolith.contracts.BudgetClient;
import com.binaryigor.modularmonolith.contracts.InventoryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@SpringBootApplication
public class CampaignApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampaignApplication.class, args);
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
    public CampaignRepository campaignRepository(JdbcTemplate campaignJdbcTemplate) {
        return new SqlCampaignRepository(campaignJdbcTemplate);
    }

    @Bean
    public DataSourceInitializer campaignDataSourceInitializer(DataSource campaignDataSource) {
        var resourceDatabasePopulator = new ResourceDatabasePopulator(new ClassPathResource("/campaign_schema.sql"));

        var dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(campaignDataSource);
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator);

        return dataSourceInitializer;
    }

    @Profile("local")
    @Bean
    public BudgetClient budgetClient() {
        return id -> true;
    }

    @Profile("local")
    @Bean
    public InventoryClient inventoryClient() {
        return id -> true;
    }
}
