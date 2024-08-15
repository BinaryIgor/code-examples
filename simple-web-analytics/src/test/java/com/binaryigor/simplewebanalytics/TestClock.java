package com.binaryigor.simplewebanalytics;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class TestClock extends Clock {

    // Postgres max precision
    private Instant time = Instant.now().truncatedTo(ChronoUnit.MICROS);

    public void moveByReasonableAmount() {
        time = time.plus(Duration.ofSeconds(1));
    }

    @Override
    public ZoneId getZone() {
        return ZoneId.of("UTC");
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return null;
    }

    @Override
    public Instant instant() {
        return time;
    }
}
