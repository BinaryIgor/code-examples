package com.binaryigor.modularpattern.user.infra;

import com.binaryigor.modularpattern.shared.contracts.UserChangedEvent;
import com.binaryigor.modularpattern.user.IntegrationTest;
import com.binaryigor.modularpattern.user.TestObjects;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class HttpUserChangedPublisherTest extends IntegrationTest {

    private static final String USER_CHANGED_EVENT_URL = "/events/" + UserChangedEvent.class.getSimpleName();

    @ParameterizedTest
    @ValueSource(ints = {400, 500, 502})
    void throwsExceptionIfGetsNon200ResponseWhilePublishing(int responseStatus) throws Exception {
        var userChangedEvent = TestObjects.randomUserChangedEvent();

        stubEventsEndpoint(userChangedEvent, responseStatus);

        Assertions.assertThatThrownBy(() -> appEventsPublisher.publish(userChangedEvent))
            .isInstanceOf(RuntimeException.class);

        verifyEventWasPublished();
    }

    @Test
    void publishesEventByMakingHttpCall() throws Exception {
        var userChangedEvent = TestObjects.randomUserChangedEvent();

        stubEventsEndpoint(userChangedEvent, 200);

        appEventsPublisher.publish(userChangedEvent);

        verifyEventWasPublished();
    }

    private void stubEventsEndpoint(UserChangedEvent requestEvent,
                                    int responseStatus) throws Exception {
        var json = objectMapper.writeValueAsString(requestEvent);

        WireMock.stubFor(WireMock.post(USER_CHANGED_EVENT_URL)
            .withRequestBody(WireMock.equalToJson(json))
            .withHeader("content-type", WireMock.equalTo("application/json"))
            .willReturn(WireMock.aResponse()
                .withStatus(responseStatus)));
    }

    private void verifyEventWasPublished() {
        WireMock.verify(1, WireMock.postRequestedFor(WireMock.urlEqualTo(USER_CHANGED_EVENT_URL)));
    }
}
