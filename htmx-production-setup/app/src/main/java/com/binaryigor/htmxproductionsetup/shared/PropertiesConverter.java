package com.binaryigor.htmxproductionsetup.shared;

import java.util.Base64;

public class PropertiesConverter {

    public static byte[] bytesFromString(String string) {
        if (string == null) {
            return null;
        }
        return Base64.getDecoder().decode(string);
    }
}
