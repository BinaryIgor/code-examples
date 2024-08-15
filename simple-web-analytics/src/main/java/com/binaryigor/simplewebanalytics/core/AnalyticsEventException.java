package com.binaryigor.simplewebanalytics.core;

public class AnalyticsEventException extends RuntimeException {

    public AnalyticsEventException(String message) {
        super(message);
    }

    public static AnalyticsEventException ofField(String field, Object value) {
        return new AnalyticsEventException("Invalid %s field: %s. Required fields have limit on size".formatted(field, value));
    }
}
