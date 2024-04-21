package com.binaryigor.htmxproductionsetup.shared;

import java.util.Base64;

public class PropertiesConverter {

    public static byte[] bytesFromBase64String(String string) {
        if (string == null || string.isBlank()) {
            throw new IllegalStateException("Can't read bytes from null or blank base64 string");
        }
        return Base64.getDecoder().decode(string);
    }
}
