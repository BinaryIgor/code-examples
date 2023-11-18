package com.binaryigor.modularmonolithsimple.campaign;

import java.util.UUID;

public class CampaignNotFoundException extends RuntimeException {

    public CampaignNotFoundException(UUID id) {
        super("Campaign of %s id doesn't exist".formatted(id));
    }
}
