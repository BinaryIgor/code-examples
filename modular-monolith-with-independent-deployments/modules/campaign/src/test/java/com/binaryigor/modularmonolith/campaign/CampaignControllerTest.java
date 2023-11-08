package com.binaryigor.modularmonolith.campaign;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.UUID;

@ActiveProfiles(value = {"campaign", "integration"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CampaignControllerTest {

    static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15");

    static {
        POSTGRESQL_CONTAINER.start();
        System.setProperty("CAMPAIGN_DB_URL", POSTGRESQL_CONTAINER.getJdbcUrl());
        System.setProperty("CAMPAIGN_DB_USERNAME", POSTGRESQL_CONTAINER.getUsername());
        System.setProperty("CAMPAIGN_DB_PASSWORD", POSTGRESQL_CONTAINER.getPassword());
    }

    @Autowired
    CampaignController controller;

    @Test
    void shouldCreateAndReturnCampaign() {
        var id = UUID.randomUUID();

        Assertions.assertThatThrownBy(() -> controller.get(id))
                .isInstanceOf(CampaignNotFoundException.class)
                .hasMessageContaining(id.toString());

        var campaign = new Campaign(id, "some-campaign",
                UUID.randomUUID(),
                UUID.randomUUID(),
                LocalDate.now(),
                null);

        controller.save(campaign);

        Assertions.assertThat(controller.get(id)).isEqualTo(campaign);
    }

}
