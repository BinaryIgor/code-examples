package com.binaryigor.htmxproductionsetup.shared.contracts;

import com.binaryigor.htmxproductionsetup.shared.Language;

import java.util.UUID;

public record UserData(UUID id,
                       String email,
                       String name,
                       Language language) {
}
