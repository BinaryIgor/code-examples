package com.binaryigor.restapitests.support;

import com.binaryigor.restapitests.api.CreateOrUpdateClientRequest;
import com.binaryigor.restapitests.domain.Client;
import com.binaryigor.restapitests.domain.ClientStatus;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class TestObjects {

    public static final CreateOrUpdateClientRequest CREATE_OR_UPDATE_CLIENT_REQUEST1 =
            new CreateOrUpdateClientRequest("client1", "client1@gmail.com", ClientStatus.FIRST_CONTACT);
    public static final CreateOrUpdateClientRequest CREATE_OR_UPDATE_CLIENT_REQUEST2 =
            new CreateOrUpdateClientRequest("client2", "client2@gmail.com", ClientStatus.SECOND_CONTACT);
    private static final Random RANDOM = new SecureRandom();

    public static List<String> invalidNames() {
        return Arrays.asList("", null, " ",
                randomString(Client.MIN_NAME_LENGTH - 1),
                randomString(Client.MAX_NAME_LENGTH + 1));
    }

    public static List<String> invalidEmails() {
        return Arrays.asList("", null, " ",
                randomString(20),
                "a@gmail.tolongextension",
                "@email.com",
                "sth@.co",
                "address@org",
                randomString(Client.MIN_EMAIL_LENGTH - 1),
                randomString(Client.MAX_EMAIL_LENGTH + 1));
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

    public static String randomString() {
        return randomString(1 + RANDOM.nextInt(50));
    }
}
