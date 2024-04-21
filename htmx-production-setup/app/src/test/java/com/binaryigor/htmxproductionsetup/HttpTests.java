package com.binaryigor.htmxproductionsetup;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.Map;

public class HttpTests {

    public static ResponseEntity<String> postForm(TestRestTemplate restTemplate,
                                                  String url,
                                                  Map<String, String> form) {
        return postForm(restTemplate, url, form, Map.of());
    }

    public static ResponseEntity<String> postForm(TestRestTemplate restTemplate,
                                                  String url,
                                                  Map<String, String> form,
                                                  Map<String, String> additionalHeaders) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        additionalHeaders.forEach(headers::add);

        var requestForm = new LinkedMultiValueMap<String, String>();
        form.forEach(requestForm::add);

        var request = new HttpEntity<MultiValueMap<String, String>>(requestForm, headers);

        return restTemplate.postForEntity(url, request, String.class);
    }
}
