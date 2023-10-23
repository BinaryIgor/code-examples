package com.binaryigor.restapitests.domain;

import java.util.UUID;
import java.util.regex.Pattern;

public record Client(UUID id,
                     String name,
                     String email,
                     ClientStatus status) {

    public static final int MIN_NAME_LENGTH = 3;
    public static final int MAX_NAME_LENGTH = 25;
    public static final int MIN_EMAIL_LENGTH = 10;
    public static final int MAX_EMAIL_LENGTH = 100;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,8}$");

    public Client {
        if (id == null) {
            throw new ClientValidationException("id is required");
        }
        if (FieldValidator.isBlankOrNotHaveLength(name, MIN_NAME_LENGTH, MAX_NAME_LENGTH)) {
            throw new ClientValidationException("Name can't be blank and must have %d - %d characters"
                    .formatted(MIN_NAME_LENGTH, MAX_NAME_LENGTH));
        }
        if (FieldValidator.isBlankOrNotHaveLength(email, MIN_EMAIL_LENGTH, MAX_EMAIL_LENGTH)
                || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ClientValidationException("Email can't be blank and must be a valid email address");
        }
        if (status == null) {
            throw new ClientValidationException("Status is required");
        }
    }
}
