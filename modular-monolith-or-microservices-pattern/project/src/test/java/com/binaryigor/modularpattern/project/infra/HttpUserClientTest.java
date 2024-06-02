package com.binaryigor.modularpattern.project.infra;

import com.binaryigor.modularpattern.project.IntegrationTest;
import com.binaryigor.modularpattern.project.TestObjects;
import com.binaryigor.modularpattern.shared.contracts.UserView;
import com.github.tomakehurst.wiremock.client.WireMock;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Stream;

public class HttpUserClientTest extends IntegrationTest {

    @Autowired
    private HttpUserClient userClient;

    @Test
    void getsAllUsers() throws Exception {
        var allUsers = Stream.generate(TestObjects::randomUserView)
            .limit(25)
            .toList();

        stubUsersEndpoint(allUsers, 200);

        Assertions.assertThat(userClient.allUsers().toList())
            .containsExactlyElementsOf(allUsers);
    }

    private void stubUsersEndpoint(List<UserView> users,
                                   int responseCode) throws Exception {
        var ndJson = new StringBuilder();
        for (var u : users) {
            ndJson.append(objectMapper.writeValueAsString(u)).append(" \n");
        }

        WireMock.stubFor(WireMock.get("/users")
            .willReturn(WireMock.aResponse()
                .withStatus(responseCode)
                .withBody(ndJson.toString())));
    }
}
