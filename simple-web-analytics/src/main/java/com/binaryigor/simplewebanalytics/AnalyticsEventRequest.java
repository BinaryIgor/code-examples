package com.binaryigor.simplewebanalytics;


import java.time.Instant;
import java.util.UUID;

public record AnalyticsEventRequest(String url,
                                    String browser,
                                    String platform,
                                    String device,
                                    String type,
                                    Object data) {

    public AnalyticsEvent toEvent(Instant timestamp, String clientIp, UUID deviceId, UUID userId) {
        return new AnalyticsEvent(timestamp, clientIp, deviceId, userId,
            url, browser, platform, device, type, data);
    }
}
