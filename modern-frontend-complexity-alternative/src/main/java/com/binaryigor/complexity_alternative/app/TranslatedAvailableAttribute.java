package com.binaryigor.complexity_alternative.app;

import com.binaryigor.complexity_alternative.domain.AvailableAttribute;

import java.util.Collection;

public record TranslatedAvailableAttribute(String name, String key, Collection<String> values) {

    public static TranslatedAvailableAttribute translated(AvailableAttribute attribute, String name) {
        return new TranslatedAvailableAttribute(name, attribute.key(), attribute.values());
    }
}
