package com.binaryigor.restapitests.domain;

public class FieldValidator {

    public static boolean isBlankOrNotHaveLength(String string, int minLength, int maxLength) {
        return string == null || string.strip().length() < minLength || string.length() > maxLength;
    }
}
