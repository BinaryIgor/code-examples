package com.binaryigor.complexity_alternative.domain;

import java.util.UUID;

public class DeviceNotFoundException extends RuntimeException {

    public DeviceNotFoundException(UUID id) {
        super("Device of %s id doesn't exist".formatted(id));
    }
}
