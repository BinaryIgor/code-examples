package com.binaryigor.smart.gateway;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Collections;
import java.util.Objects;

@RestController
public class SmartGatewayController {

    private final RestClient restClient;
    private final Routes routes;

    public SmartGatewayController(RestClient restClient,
                                  Routes routes) {
        this.restClient = restClient;
        this.routes = routes;
    }

    @PostConstruct
    void postConstruct() {
        System.out.println("Supported routes..." + routes);
    }

    @RequestMapping(path = "/api/**")
    ResponseEntity<Object> gateway(HttpServletRequest request,
                                   @RequestBody(required = false) Object body) {
        var uri = request.getServletPath().replace("/api", "");
        System.out.println("Uri to check..." + uri);
        var routeOpt = routes.routes().stream()
                .filter(r -> uri.startsWith(r.pathPattern()))
                .findAny();

        if (routeOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("%s uri is not supported. Only %s are supported".formatted(uri, routes.routes()));
        }

        System.out.println("Request: " + request);
        var proxiedReq = restClient.method(HttpMethod.valueOf(request.getMethod()))
                .uri(routeOpt.get().uri() + "/" + uri)
                .headers(hs -> {
                    Collections.list(request.getHeaderNames())
                            .forEach(h -> {
                                hs.addAll(h, Collections.list(request.getHeaders(h)));
                            });
                });

        if (body != null) {
            proxiedReq = proxiedReq.body(body);
        }

        try {

            var proxiedRes = proxiedReq.retrieve()
                    .toEntity(byte[].class);

            var proxiedBody = proxiedRes.hasBody() ? Objects.requireNonNull(proxiedRes.getBody()) : null;

            System.out.println(proxiedRes.getStatusCode());
//            System.out.println(new String(proxiedBody));

            return ResponseEntity.status(proxiedRes.getStatusCode())
                    .headers(proxiedRes.getHeaders())
                    .body(proxiedBody);
        } catch (RestClientResponseException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(e.getResponseBodyAsString());
        } catch (Exception e) {
            System.err.println("Failed to complete request, unknown exception" + e.getClass());
            e.printStackTrace();
            throw e;
        }
    }
}
