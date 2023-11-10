package com.binaryigor.modularmonolith.campaign;

import com.binaryigor.modularmonolith.contracts.BudgetClient;
import com.binaryigor.modularmonolith.contracts.InventoryClient;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CampaignService {

    private final CampaignRepository campaignRepository;
    private final BudgetClient budgetClient;
    private final InventoryClient inventoryClient;

    public CampaignService(CampaignRepository campaignRepository,
                           BudgetClient budgetClient,
                           InventoryClient inventoryClient) {
        this.campaignRepository = campaignRepository;
        this.budgetClient = budgetClient;
        this.inventoryClient = inventoryClient;
    }

    public void save(Campaign campaign) {
        if (!budgetClient.budgetExists(campaign.budgetId())) {
            throw new CampaignValidationException("Campaign doesn't have a budget. Create it first");
        }

        if (!inventoryClient.inventoryExists(campaign.inventoryId())) {
            throw new CampaignValidationException("Campaign doesn't have an inventory. Create it first");
        }

        campaignRepository.save(campaign);
    }

    public Campaign findById(UUID id) {
        return campaignRepository.findById(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));
    }
}
