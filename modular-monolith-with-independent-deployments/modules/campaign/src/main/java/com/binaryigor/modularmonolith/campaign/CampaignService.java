package com.binaryigor.modularmonolith.campaign;

import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;

    public CampaignService(CampaignRepository campaignRepository) {
        this.campaignRepository = campaignRepository;
    }

    //TODO: validate
    public void save(Campaign campaign) {
        campaignRepository.save(campaign);
    }

    public Campaign findById(UUID id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));
    }
}
