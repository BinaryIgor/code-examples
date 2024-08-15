package com.binaryigor.simplewebanalytics.core;

import java.time.Instant;
import java.util.UUID;

public record AnalyticsEvent(Instant timestamp,
                             String ip,
                             UUID deviceId,
                             UUID userId,
                             String url,
                             String browser,
                             String os,
                             String device,
                             String type,
                             Object data) {
}
