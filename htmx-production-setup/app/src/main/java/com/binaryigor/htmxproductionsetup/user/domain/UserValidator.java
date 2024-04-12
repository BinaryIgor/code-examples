package com.binaryigor.htmxproductionsetup.user.domain;

import com.binaryigor.htmxproductionsetup.shared.FieldValidator;
import com.binaryigor.htmxproductionsetup.user.domain.exception.InvalidEmailException;
import com.binaryigor.htmxproductionsetup.user.domain.exception.InvalidPasswordException;

public class UserValidator {

    public static final int MAX_EMAIL_LENGTH = 150;
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 100;

    public static boolean isEmailValid(String email) {
        var invalid = email == null ||
                FieldValidator.hasHtmlCharacters(email) ||
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
        var dotIndex = domain.indexOf(".");

        return dotIndex > 0 && dotIndex < (domain.length() - 1);
    }

    public static void validateEmail(String email) {
        if (!isEmailValid(email)) {
            throw new InvalidEmailException(email);
        }
    }

    public static boolean isPasswordValid(String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }
        var alpha = false;
        var digit = false;
        var upper = false;
        var lower = false;

        for (var c : password.toCharArray()) {
            if (Character.isAlphabetic(c) && !alpha) {
                alpha = true;
            }
            if (Character.isUpperCase(c) && !upper) {
                upper = true;
            }
            if (Character.isLowerCase(c) && !lower) {
                lower = true;
            }
            if (Character.isDigit(c) && !digit) {
                digit = true;
            }
        }

        return alpha && digit && upper && lower;
    }

    public static void validatePassword(String password) {
        if (!isPasswordValid(password)) {
            throw new InvalidPasswordException();
        }
    }

}
