package com.binaryigor.htmxvsreact.user.domain;

import com.binaryigor.htmxvsreact.shared.FieldValidator;

public class UserValidator {

    public static final int MAX_EMAIL_LENGTH = 150;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 50;

    public static boolean isEmailValid(String email) {
        var invalid = email == null ||
                      email.length() > MAX_EMAIL_LENGTH;
        if (invalid) {
            return false;
        }

        var atIdx = email.indexOf("@");
        if (atIdx < 1) {
            return false;
        }

        var name = email.substring(0, atIdx);
        if (!FieldValidator.hasAtLeastOneLetterOrDigit(name)) {
            return false;
        }

        var domain = email.substring(atIdx + 1);
        return !domain.isBlank();
    }

    public static boolean isPasswordValid(String password) {
        return FieldValidator.hasLengthBetween(password, MIN_PASSWORD_LENGTH, MAX_PASSWORD_LENGTH);
    }
}
