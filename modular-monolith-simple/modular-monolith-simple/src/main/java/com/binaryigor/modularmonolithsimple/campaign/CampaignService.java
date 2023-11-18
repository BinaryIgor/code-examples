package com.binaryigor.modularmonolithsimple.campaign;

import com.binaryigor.modularmonolithsimple._contracts.BudgetClient;
import com.binaryigor.modularmonolithsimple._contracts.BudgetSavedEvent;
import com.binaryigor.modularmonolithsimple._contracts.InventoryClient;
import com.binaryigor.modularmonolithsimple._contracts.InventorySavedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CampaignService {

    private static final Logger log = LoggerFactory.getLogger(CampaignService.class);
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

    @EventListener
    public void onBudgetSaved(BudgetSavedEvent event) {
        log.info("Budget has been saved: {}", event);
    }

    @EventListener
    public void onInventorySaved(InventorySavedEvent event) {
        log.info("Inventory has been saved: {}", event);
    }
}
