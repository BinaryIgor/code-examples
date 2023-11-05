package com.binaryigor.modularmonolith.campaign;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/campaigns")
public class CampaignController {


    @PutMapping
    void save(Campaign campaign) {
        System.out.println("Saving campaign..." + campaign);
    }

    @GetMapping("{id}")
    Campaign get(@PathVariable UUID id) {
        return new Campaign(id, "some name", LocalDate.now(), null);
    }
}
