package com.binaryigor.modularmonolith.campaign;

import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/campaigns")
public class CampaignController {

    private final Clock clock;

    public CampaignController(Clock clock) {
        this.clock = clock;
    }

    @PutMapping
    void save(Campaign campaign) {
        System.out.println("Saving %s campaign on %s".formatted(clock.instant(), campaign));
    }

    @GetMapping("{id}")
    Campaign get(@PathVariable(name = "id") UUID id) {
        return new Campaign(id, "some name", LocalDate.now(), null);
    }
}
