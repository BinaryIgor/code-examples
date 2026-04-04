package com.binaryigor.complexity_alternative.domain;

import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public record DeviceOffer(UUID id,
                          UUID deviceId,
                          Map<String, String> attributes,
                          Money price,
                          UUID merchantId,
                          Instant expiresAt) {

    public Collection<String> attributeValues() {
        return attributes.values();
    }
}
