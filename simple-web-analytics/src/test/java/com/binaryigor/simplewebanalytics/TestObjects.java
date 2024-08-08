package com.binaryigor.simplewebanalytics;

import com.binaryigor.simplewebanalytics.core.AnalyticsEvent;
import com.binaryigor.simplewebanalytics.web.AnalyticsEventRequest;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

public class TestObjects {

    private static final Random RANDOM = new SecureRandom();

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    public static AnalyticsEventRequest randomAnalyticsEventRequest() {
        return new AnalyticsEventRequest(UUID.randomUUID(), "https://" + randomString(),
            randomString(), randomString(), randomString(), randomString(),
            // accepted SqlAnalyticsEventRepository data type dependency - for tests only
            RANDOM.nextBoolean() ? null : Map.of("eventId", UUID.randomUUID().toString()));
    }

    public static AnalyticsEvent randomAnalyticsEvent() {
        return randomAnalyticsEventRequest().toEvent(Instant.now(), randomString(),
            RANDOM.nextBoolean() ? null : UUID.randomUUID());
    }

    public static List<String> invalidAnalyticsEventFieldCases(int maxSize) {
        return Arrays.asList("", " ", null, "a" + "b".repeat(maxSize));
    }
}
