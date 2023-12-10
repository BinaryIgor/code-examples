package com.binaryigor.modularmonolithsimple.campaign;

import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/campaigns")
public class CampaignController {

    private final CampaignService campaignService;

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
    }

    @PutMapping
    void save(@RequestBody Campaign campaign) {
        campaignService.save(campaign);
    }

    @GetMapping("{id}")
    Campaign get(@PathVariable(name = "id") UUID id) {
        return campaignService.findById(id);
    }
}
