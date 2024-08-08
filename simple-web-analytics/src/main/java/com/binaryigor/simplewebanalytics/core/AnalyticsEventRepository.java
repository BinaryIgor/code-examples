package com.binaryigor.simplewebanalytics.core;

import java.util.Collection;

public interface AnalyticsEventRepository {
    void create(Collection<AnalyticsEvent> events);
}
