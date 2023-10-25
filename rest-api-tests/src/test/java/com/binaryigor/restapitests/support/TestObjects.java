package com.binaryigor.restapitests.support;

import com.binaryigor.restapitests.api.CreateOrUpdateClientRequest;
import com.binaryigor.restapitests.domain.Client;
import com.binaryigor.restapitests.domain.ClientStatus;

import java.util.UUID;
import java.util.stream.Stream;

public class TestObjects {

    public static CreateOrUpdateClientRequest createOrUpdateClientRequest1() {
        return new CreateOrUpdateClientRequest("client1", "client1@email.com",
                ClientStatus.TO_CONTACT);
    }

    public static CreateOrUpdateClientRequest createOrUpdateClientRequest2() {
        return new CreateOrUpdateClientRequest("client2", "client2@email.com",
                ClientStatus.OFFER_ACCEPTED);
    }

    public static Stream<String> invalidClientNames() {
        return Stream.of(null, "", " ",
                randomString(Client.MIN_NAME_LENGTH - 1),
                randomString(Client.MAX_NAME_LENGTH + 1));
    }

    public static Stream<String> invalidClientEmails() {
        return Stream.of(null, "", " ",
                randomString(Client.MIN_EMAIL_LENGTH - 1),
                randomString(Client.MAX_EMAIL_LENGTH + 1),
                "@gmail.com",
                "sth.gmail.com",
                "valid@gmail.tolongextension",
                "I*ad@email.co");
    }

    public static String randomString(int length) {
        if (length <= 0) {
            return "";
        }

        var string = new StringBuilder();
        while (string.length() < length) {
            string.append(UUID.randomUUID());
        }
        return string.substring(0, length);
    }

}
