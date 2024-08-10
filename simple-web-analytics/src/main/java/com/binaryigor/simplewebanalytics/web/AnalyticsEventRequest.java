package com.binaryigor.simplewebanalytics.web;

import com.binaryigor.simplewebanalytics.core.AnalyticsEvent;

import java.time.Instant;
import java.util.UUID;

public record AnalyticsEventRequest(UUID deviceId,
                                    String url,
                                    String browser,
                                    String os,
                                    String device,
                                    String type,
                                    Object data) {

    public AnalyticsEvent toEvent(Instant timestamp, String clientIp, UUID userId) {
        return new AnalyticsEvent(timestamp, clientIp, deviceId, userId,
            url, browser, os, device, type, data);
    }
}
