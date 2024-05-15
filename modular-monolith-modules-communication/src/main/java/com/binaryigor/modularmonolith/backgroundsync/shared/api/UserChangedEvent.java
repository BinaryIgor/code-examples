package com.binaryigor.modularmonolith.backgroundsync.shared.api;

import java.util.UUID;

public record UserChangedEvent(UUID id, String name, String email) { }
