package com.binaryigor.modularpattern.shared;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

public class TestClock extends Clock {

    private Instant instant;

    public TestClock() {
        this.instant = Instant.now();
    }

    public void setTime(Instant instant) {
        this.instant = instant;
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
        return instant;
    }
}
