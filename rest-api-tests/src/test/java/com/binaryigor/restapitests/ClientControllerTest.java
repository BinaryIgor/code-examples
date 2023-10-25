package com.binaryigor.restapitests;

import com.binaryigor.restapitests.api.CreateClientResponse;
import com.binaryigor.restapitests.api.CreateOrUpdateClientRequest;
import com.binaryigor.restapitests.api.ErrorResponse;
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

public class ClientControllerTest extends IntegrationTest {

    @ParameterizedTest
    @MethodSource("invalidCreateOrUpdateRequests")
    void shouldNotAllowToCreateInvalidClient(CreateOrUpdateClientRequest request) {
        var response = createClient(request);
        assertErrorResponse(response, HttpStatus.BAD_REQUEST, ClientValidationException.class);
    }

    @Test
    void shouldAddAndGetClient() {
        var createClientRequest = TestObjects.createOrUpdateClientRequest1();

        var createClientResponse = createClient(createClientRequest);

        assertResponseStatus(createClientResponse, HttpStatus.CREATED);

        var clientId = createClientResponse.bodyAsJson(CreateClientResponse.class).id();

        var getClientResponse = getClient(clientId);

        var expectedClient = createClientRequest.toClient(clientId);

        assertResponseStatus(getClientResponse, HttpStatus.OK);
        assertResponseBody(getClientResponse, expectedClient);
    }

    @Test
    void shouldNotGetNonexistentClient() {
        var nonexistentClientId = UUID.randomUUID();

        var response = getClient(nonexistentClientId);

        assertResponseStatus(response, HttpStatus.NOT_FOUND);
        assertResponseBody(response,
                ErrorResponse.fromException(new ClientNotFoundException(nonexistentClientId)));
    }

    @Test
    void shouldNotUpdateNonexistentClient() {
        var nonexistentClientId = UUID.randomUUID();

        var response = updateClient(nonexistentClientId, TestObjects.createOrUpdateClientRequest1());

        assertResponseStatus(response, HttpStatus.NOT_FOUND);
        assertResponseBody(response,
                ErrorResponse.fromException(new ClientNotFoundException(nonexistentClientId)));
    }

    @ParameterizedTest
    @MethodSource("invalidCreateOrUpdateRequests")
    void shouldNotAllowToUpdateInvalidClient(CreateOrUpdateClientRequest updateRequest) {
        var clientId = createClientReturningId(TestObjects.createOrUpdateClientRequest1());

        var response = updateClient(clientId, updateRequest);

        assertErrorResponse(response, HttpStatus.BAD_REQUEST, ClientValidationException.class);
    }

    @Test
    void shouldUpdateClient() {
        var createClientRequest = TestObjects.createOrUpdateClientRequest1();
        var updateClientRequest = TestObjects.createOrUpdateClientRequest2();

        var clientId = createClientReturningId(createClientRequest);

        var expectedClientAfterUpdate = updateClientRequest.toClient(clientId);

        var updateResponse = updateClient(clientId, updateClientRequest);

        assertResponseStatus(updateResponse, HttpStatus.OK);

        assertResponseBody(getClient(clientId), expectedClientAfterUpdate);
    }

    @Test
    void shouldNotAllowToUpdateClientWithTakenEmail() {
        var createClientRequest1 = TestObjects.createOrUpdateClientRequest1();
        var createClientRequest2 = TestObjects.createOrUpdateClientRequest2();

        var client1Id = createClientReturningId(createClientRequest1);
        var client2Id = createClientReturningId(createClientRequest2);

        var updateClient2RequestToClient1Email = createClientRequest1;

        var updateClient2Response = updateClient(client2Id, updateClient2RequestToClient1Email);

        assertErrorResponse(updateClient2Response, HttpStatus.BAD_REQUEST, ClientValidationException.class);
    }


    private TestHttpResponse createClient(CreateOrUpdateClientRequest request) {
        return testHttpClient.request()
                .path("clients")
                .POST()
                .body(request)
                .execute();
    }

    private UUID createClientReturningId(CreateOrUpdateClientRequest request) {
        return createClient(request)
                .bodyAsJson(CreateClientResponse.class)
                .id();
    }

    private TestHttpResponse getClient(UUID id) {
        return testHttpClient.request()
                .path("clients/" + id)
                .GET()
                .execute();
    }

    private TestHttpResponse updateClient(UUID id, CreateOrUpdateClientRequest request) {
        return testHttpClient.request()
                .path("clients/" + id)
                .PUT()
                .body(request)
                .execute();
    }

    static Stream<CreateOrUpdateClientRequest> invalidCreateOrUpdateRequests() {
        var invalidNameRequests = TestObjects.invalidClientNames()
                .map(invalidName -> new CreateOrUpdateClientRequest(invalidName, "client@email.com",
                        ClientStatus.FIRST_CONTACT));

        var invalidEmailRequests = TestObjects.invalidClientEmails()
                .map(invalidEmail -> new CreateOrUpdateClientRequest("some-client", invalidEmail,
                        ClientStatus.TO_CONTACT));

        var otherInvalidRequests = Stream.of(
                new CreateOrUpdateClientRequest(null, null, null),
                new CreateOrUpdateClientRequest("valid-name", "valid-email@email.com", null));

        return Stream.of(invalidNameRequests, invalidEmailRequests, otherInvalidRequests)
                .flatMap(Function.identity());
    }
}
