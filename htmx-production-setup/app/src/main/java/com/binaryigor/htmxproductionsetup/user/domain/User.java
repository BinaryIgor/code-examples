package com.binaryigor.htmxproductionsetup.user.domain;

import com.binaryigor.htmxproductionsetup.shared.Language;

import java.util.UUID;

public record User(UUID id, String email, String name, String password, Language language) {
}
