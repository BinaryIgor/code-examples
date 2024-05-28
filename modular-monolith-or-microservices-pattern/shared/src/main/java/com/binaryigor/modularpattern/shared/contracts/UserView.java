package com.binaryigor.modularpattern.shared.contracts;

import java.util.UUID;

public record UserView(UUID id, String email, String name, long version) {
}
