package com.binaryigor.htmxproductionsetup.user.domain;

import org.springframework.stereotype.Component;

@Component
public class DumbPasswordHasher implements PasswordHasher {

    @Override
    public String hash(String password) {
        return password;
    }

    @Override
    public boolean matches(String rawPassword, String hashedPassword) {
        return rawPassword.equals(hashedPassword);
    }
}
