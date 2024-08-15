package com.binaryigor.simplewebanalytics;

import java.util.Optional;
import java.util.UUID;

public interface UserAuth {
    Optional<UUID> currentUserId();
}
