package com.binaryigor.single.app.account;

import java.time.Instant;
import java.util.UUID;

public record Account(UUID id, String name, String email, Instant createdAt) {

}
