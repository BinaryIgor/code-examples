package com.binaryigor.modularmonolith.backgroundsync.shared.api;

import java.util.UUID;

public record UserView(UUID id, String name, String email) { }
