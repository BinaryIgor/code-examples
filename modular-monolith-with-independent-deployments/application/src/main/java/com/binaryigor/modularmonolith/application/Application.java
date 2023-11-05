package com.binaryigor.modularmonolith.application;

import com.binaryigor.modularmonolith.budget.BudgetModule;
import com.binaryigor.modularmonolith.campaign.CampaignModule;

public class Application {
    public static void main(String[] args) {
        var campaignModule = new CampaignModule();
        var budgetModule = new BudgetModule();

        campaignModule.start();
        budgetModule.start();
    }
}
