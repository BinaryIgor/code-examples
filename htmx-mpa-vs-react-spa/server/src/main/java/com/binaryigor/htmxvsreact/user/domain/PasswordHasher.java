package com.binaryigor.htmxvsreact.user.domain;

public interface PasswordHasher {

    String hash(String password);

    boolean matches(String rawPassword, String hashedPassword);
}
