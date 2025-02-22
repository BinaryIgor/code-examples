package com.binaryigor.htmxvsreact.shared;

import java.util.regex.Pattern;

public class FieldValidator {

    private static final Pattern HTML_CHARACTERS_REGEX = Pattern.compile("(?s)(.*)<(.+)>(?s)(.*)");

    public static boolean hasAtLeastOneLetterOrDigit(String string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isLetter(c) || Character.isDigit(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNameValid(String name, int minLength, int maxLength) {
        var invalid = name == null ||
                      name.strip().length() < minLength || name.length() > maxLength ||
                      hasHtmlCharacters(name);
        if (invalid) {
            return false;
        }

        return hasAtLeastOneLetter(name);
    }

    private static boolean hasAtLeastOneLetter(String string) {
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (Character.isLetter(c)) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasAnyContent(String string) {
        return !(string == null || string.isBlank());
    }

    public static boolean isLongerThan(String string, int length) {
        return string != null && string.length() > length;
    }

    public static boolean hasLengthBetween(String string, int min, int max) {
        return string != null && string.length() >= min && string.length() <= max;
    }

    public static boolean hasHtmlCharacters(String string) {
        return string != null && HTML_CHARACTERS_REGEX.matcher(string).matches();
    }

}
