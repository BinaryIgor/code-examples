package com.binaryigor.simplewebanalytics.web;

import com.binaryigor.simplewebanalytics.IntegrationTest;
import com.binaryigor.simplewebanalytics.TestObjects;
import com.binaryigor.simplewebanalytics.core.AnalyticsEvent;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class AnalyticsControllerTest extends IntegrationTest {

    @Value("${analytics-events.check-batch-delay}")
    int checkBatchDelay;

    @Test
    void addsAnalyticsEvents() {
        var event1 = TestObjects.randomAnalyticsEventRequest();
        var event2 = TestObjects.randomAnalyticsEventRequest();
        var event3 = TestObjects.randomAnalyticsEventRequest();

        var ip1 = "127.0.0.1";
        var ip2 = "132.22.33.1";
        var ip3 = "138.101.22.1";

        var user2 = UUID.randomUUID();
        var user3 = UUID.randomUUID();

        addAnalyticsEvent(event1, null);
        var expectedEvent1 = expectedEvent(event1, ip1, null);

        testClock.moveByReasonableAmount();
        addAnalyticsEvent(event2, user2, ip2);
        var expectedEvent2 = expectedEvent(event2, ip2, user2);

        testClock.moveByReasonableAmount();
        addAnalyticsEvent(event3, user3, ip3);
        var expectedEvent3 = expectedEvent(event3, ip3, user3);

        delay(checkBatchDelay);

        assertEventsWereAdded(expectedEvent1, expectedEvent2, expectedEvent3);
    }

    private void addAnalyticsEvent(AnalyticsEventRequest request, UUID userId, String ipHeader) {
        var headers = new HttpHeaders();
        if (userId != null) {
            headers.set("user-id", userId.toString());
        }
        if (ipHeader != null) {
            headers.set("real-ip", ipHeader);
        }

        var httpEntity = new HttpEntity<>(request, headers);

        var response = restTemplate.exchange("/analytics/events", HttpMethod.POST, httpEntity, Void.class);

        System.out.println(response);

        Assertions.assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.ACCEPTED);
    }

    private void addAnalyticsEvent(AnalyticsEventRequest request, UUID userId) {
        addAnalyticsEvent(request, userId, null);
    }

    private AnalyticsEvent expectedEvent(AnalyticsEventRequest request, String clientIp, UUID userId) {
        return request.toEvent(testClock.instant(), clientIp, userId);
    }
}
