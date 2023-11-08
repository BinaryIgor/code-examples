package com.binaryigor.modularmonolith.campaign;

import java.util.Optional;
import java.util.UUID;

public interface CampaignRepository {

    void save(Campaign campaign);

    Optional<Campaign> findById(UUID id);
}
