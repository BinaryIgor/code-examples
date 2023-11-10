package com.binaryigor.modularmonolith.campaign;

import com.binaryigor.modularmonolith.contracts.ErrorResponse;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.UUID;

@ActiveProfiles(value = {"campaign", "integration"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        classes = CampaignControllerTest.TestConfig.class)
public class CampaignControllerTest {

    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15");

    static {
        POSTGRESQL_CONTAINER.start();
        System.setProperty("CAMPAIGN_DB_URL", POSTGRESQL_CONTAINER.getJdbcUrl());
        System.setProperty("CAMPAIGN_DB_USERNAME", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("CAMPAIGN_DB_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
    }

    @Autowired
    TestRestTemplate restTemplate;
    @Autowired
    TestBudgetClient testBudgetClient;
    @Autowired
    TestInventoryClient testInventoryClient;
    @Autowired
    JdbcTemplate jdbcTemplate;

    @AfterEach
    void tearDown() {
        jdbcTemplate.execute("TRUNCATE campaign;");
    }

    @Test
    void shouldCreateAndReturnCampaign() {
        var campaign = randomCampaign();

        var nonexistentCampaignResponse = getCampaign(campaign.id(), ErrorResponse.class);

        Assertions.assertThat(nonexistentCampaignResponse.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(nonexistentCampaignResponse.getBody())
                .isEqualTo(ErrorResponse.fromException(new CampaignNotFoundException(campaign.id())));

        testBudgetClient.addBudget(campaign.budgetId());
        testInventoryClient.addInventory(campaign.inventoryId());

        Assertions.assertThat(saveCampaign(campaign).getStatusCode())
                .isEqualTo(HttpStatus.OK);

        var saveCampaignResponse = getCampaign(campaign.id());

        Assertions.assertThat(saveCampaignResponse.getStatusCode())
                .isEqualTo(HttpStatus.OK);
        Assertions.assertThat(saveCampaignResponse.getBody())
                .isEqualTo(campaign);
    }

    @Test
    void shouldNotCreateCampaignWithMissingBudget() {
        var campaign = randomCampaign();

        var response = saveCampaign(campaign, ErrorResponse.class);

        expectCampaignValidationError(response, "budget");
    }

    @Test
    void shouldNotCreateCampaignWithMissingInventory() {
        var campaign = randomCampaign();

        testBudgetClient.addBudget(campaign.budgetId());

        var response = saveCampaign(campaign, ErrorResponse.class);

        expectCampaignValidationError(response, "inventory");
    }

    private Campaign randomCampaign() {
        return new Campaign(UUID.randomUUID(), "some-campaign", UUID.randomUUID(), UUID.randomUUID(),
                LocalDate.now(), null);
    }

    private <T> ResponseEntity<T> getCampaign(UUID id, Class<T> response) {
        return restTemplate.getForEntity("/campaigns/" + id, response);
    }

    private ResponseEntity<Campaign> getCampaign(UUID id) {
        return getCampaign(id, Campaign.class);
    }

    private <T> ResponseEntity<T> saveCampaign(Campaign campaign, Class<T> response) {
        return restTemplate.exchange(RequestEntity.put("/campaigns").body(campaign), response);
    }

    private ResponseEntity<Void> saveCampaign(Campaign campaign) {
        return saveCampaign(campaign, Void.class);
    }

    private void expectCampaignValidationError(ResponseEntity<ErrorResponse> response,
                                               String messageContains) {
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        Assertions.assertThat(response.getBody().error())
                .isEqualTo(ErrorResponse.exceptionAsError(CampaignValidationException.class));
        Assertions.assertThat(response.getBody().message())
                .contains(messageContains);
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        TestBudgetClient testBudgetClient() {
            return new TestBudgetClient();
        }

        @Bean
        TestInventoryClient testInventoryClient() {
            return new TestInventoryClient();
        }
    }
}
