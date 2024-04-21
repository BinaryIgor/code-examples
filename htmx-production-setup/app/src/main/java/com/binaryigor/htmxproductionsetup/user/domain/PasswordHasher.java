package com.binaryigor.htmxproductionsetup.user.domain;

public interface PasswordHasher {

    String hash(String password);

    boolean matches(String rawPassword, String hashedPassword);
}
