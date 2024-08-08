package com.binaryigor.simplewebanalytics.web;

import com.binaryigor.simplewebanalytics.UserAuth;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;
import java.util.UUID;

public class HeaderUserAuth implements UserAuth {

    @Override
    public Optional<UUID> currentUserId() {
        var requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes sa) {
            return Optional.ofNullable(sa.getRequest().getHeader("user-id"))
                .map(UUID::fromString);
        }
        return Optional.empty();
    }
}
