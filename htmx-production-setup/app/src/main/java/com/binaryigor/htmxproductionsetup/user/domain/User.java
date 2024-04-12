package com.binaryigor.htmxproductionsetup.user.domain;

import java.util.UUID;

public record User(UUID id, String email, String name, String password) {
}
