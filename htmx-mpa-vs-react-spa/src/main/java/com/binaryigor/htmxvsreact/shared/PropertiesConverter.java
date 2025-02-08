package com.binaryigor.htmxvsreact.shared;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

public class PropertiesConverter {

    private static final String FILE_PREFIX = "file:";

    public static byte[] bytesFromBase64String(String string) {
        if (string == null || string.isBlank()) {
            throw new IllegalArgumentException("Can't return bytes from null or empty string!");
        }
        return Base64.getDecoder().decode(string);
    }

    public static String valueOrFromFile(String property) {
        try {
            if (property.startsWith(FILE_PREFIX)) {
                return Files.readString(Path.of(property.replaceFirst(FILE_PREFIX, ""))).strip();
            }
            return property;
        } catch (Exception e) {
            throw new RuntimeException("Problem while reading %s property...".formatted(property), e);
        }
    }
}
