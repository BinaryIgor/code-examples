package com.binaryigor.modularmonolithsimple.campaign;

import java.util.Optional;
import java.util.UUID;

public interface CampaignRepository {

    void save(Campaign campaign);

    Optional<Campaign> findById(UUID id);
}
