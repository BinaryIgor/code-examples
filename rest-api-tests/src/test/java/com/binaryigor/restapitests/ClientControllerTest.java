package com.binaryigor.restapitests;

import com.binaryigor.restapitests.api.CreateClientResponse;
import com.binaryigor.restapitests.api.CreateOrUpdateClientRequest;
import com.binaryigor.restapitests.domain.Client;
import com.binaryigor.restapitests.domain.ClientNotFoundException;
import com.binaryigor.restapitests.domain.ClientStatus;
import com.binaryigor.restapitests.domain.ClientValidationException;
import com.binaryigor.restapitests.support.IntegrationTest;
import com.binaryigor.restapitests.support.TestHttpResponse;
import com.binaryigor.restapitests.support.TestObjects;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

class ClientControllerTest extends IntegrationTest {

    private static final String ENDPOINTS_PREFIX = "/clients";

    @ParameterizedTest
    @MethodSource("invalidCreateOrUpdateClientRequests")
    void shouldNotAllowToAddInvalidClient(CreateOrUpdateClientRequest request) {
        var response = createClient(request);
        assertErrorResponse(response, HttpStatus.BAD_REQUEST, ClientValidationException.class);
    }

    @Test
    void shouldAddAndReturnNewClient() {
        var createClientRequest = TestObjects.CREATE_OR_UPDATE_CLIENT_REQUEST1;

        var createClientResponse = createClient(createClientRequest);

        assertResponseStatus(createClientResponse, HttpStatus.CREATED);

        var clientId = createClientResponse.bodyAsJson(CreateClientResponse.class).id();

        var getClientResponse = getClient(clientId);

        var expectedCreatedClient = createClientRequest.toClient(clientId);

        assertResponseStatus(getClientResponse, HttpStatus.OK);
        assertResponseBody(getClientResponse, expectedCreatedClient, Client.class);
    }

    @Test
    void shouldNotReturnNonexistentClient() {
        var response = getClient(UUID.randomUUID());

        assertErrorResponse(response, HttpStatus.NOT_FOUND, ClientNotFoundException.class);
    }

    @Test
    void shouldNotAllowToUpdateNonexistentClient() {
        var nonexistentClientId = UUID.randomUUID();
        var updateRequest = TestObjects.CREATE_OR_UPDATE_CLIENT_REQUEST1;

        var updateResponse = updateClient(nonexistentClientId, updateRequest);

        assertErrorResponse(updateResponse, HttpStatus.NOT_FOUND, ClientNotFoundException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidCreateOrUpdateClientRequests")
    void shouldNotAllowToUpdateClientGivenInvalidRequest(CreateOrUpdateClientRequest updateRequest) {
        var createRequest = TestObjects.CREATE_OR_UPDATE_CLIENT_REQUEST1;

        var clientId = createClientReturningId(createRequest);

        var updateClientResponse = updateClient(clientId, updateRequest);

        assertErrorResponse(updateClientResponse, HttpStatus.BAD_REQUEST, ClientValidationException.class);
    }

    @Test
    void shouldNotAllowToUpdateClientWithAlreadyTakenEmail() {
        var client1Id = createClientReturningId(TestObjects.CREATE_OR_UPDATE_CLIENT_REQUEST1);
        var client2Id = createClientReturningId(TestObjects.CREATE_OR_UPDATE_CLIENT_REQUEST2);

        var updateResponse = updateClient(client2Id, TestObjects.CREATE_OR_UPDATE_CLIENT_REQUEST1);

        assertErrorResponse(updateResponse, HttpStatus.BAD_REQUEST, ClientValidationException.class);
    }

    @Test
    void shouldAllowToUpdateClient() {
        var client1Id = createClientReturningId(TestObjects.CREATE_OR_UPDATE_CLIENT_REQUEST1);
        var client2Id = createClientReturningId(TestObjects.CREATE_OR_UPDATE_CLIENT_REQUEST2);

        var client1UpdateRequest = new CreateOrUpdateClientRequest(
                "another-name1",
                "another-email1@email.com",
                ClientStatus.TO_CONTACT);
        var client2UpdateRequest = new CreateOrUpdateClientRequest(
                "another-name2",
                "another-email2@email.com",
                ClientStatus.OFFER_ACCEPTED);

        var client1UpdateResponse = updateClient(client1Id, client1UpdateRequest);
        var client2UpdateResponse = updateClient(client2Id, client2UpdateRequest);

        assertResponseStatus(client1UpdateResponse, HttpStatus.OK);
        assertResponseBody(getClient(client1Id), client1UpdateRequest.toClient(client1Id), Client.class);

        assertResponseStatus(client2UpdateResponse, HttpStatus.OK);
        assertResponseBody(getClient(client2Id), client2UpdateRequest.toClient(client2Id), Client.class);
    }

    private TestHttpResponse createClient(CreateOrUpdateClientRequest request) {
        return httpClient.request()
                .path(ENDPOINTS_PREFIX)
                .POST()
                .body(request)
                .execute();
    }

    private UUID createClientReturningId(CreateOrUpdateClientRequest request) {
        return createClient(request).bodyAsJson(CreateClientResponse.class).id();
    }

    private TestHttpResponse getClient(UUID id) {
        return httpClient.request()
                .path(ENDPOINTS_PREFIX + "/" + id)
                .GET()
                .execute();
    }

    private TestHttpResponse updateClient(UUID id, CreateOrUpdateClientRequest request) {
        return httpClient.request()
                .path(ENDPOINTS_PREFIX + "/" + id)
                .PUT()
                .body(request)
                .execute();
    }

    static Stream<CreateOrUpdateClientRequest> invalidCreateOrUpdateClientRequests() {
        var invalidNameCases = TestObjects.invalidNames().stream()
                .map(n -> new CreateOrUpdateClientRequest(n, "some-email@gmail.com", ClientStatus.FIRST_CONTACT));

        var invalidEmailCases = TestObjects.invalidEmails().stream()
                .map(e -> new CreateOrUpdateClientRequest("some name", e, ClientStatus.OFFER_ACCEPTED));

        var otherInvalidCases = Stream.of(
                new CreateOrUpdateClientRequest("name", "email@email.com", null),
                new CreateOrUpdateClientRequest(" ", null, null));

        return Stream.of(invalidNameCases, invalidEmailCases, otherInvalidCases)
                .flatMap(Function.identity());
    }

}
