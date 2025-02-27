package com.binaryigor.htmxvsreact.user.domain;

public record SignedInUser(User user, AuthToken token) {
}
