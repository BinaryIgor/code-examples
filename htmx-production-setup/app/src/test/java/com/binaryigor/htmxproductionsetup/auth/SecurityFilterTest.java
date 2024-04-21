package com.binaryigor.htmxproductionsetup.auth;

import com.binaryigor.htmxproductionsetup.HTMLAssertions;
import com.binaryigor.htmxproductionsetup.IntegrationTest;
import com.binaryigor.htmxproductionsetup.shared.Language;
import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class SecurityFilterTest extends IntegrationTest {

    @BeforeEach
    void setup() {
        Translations.setCurrentLanguage(Language.EN);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/", "/day", "/history"})
    void redirectsNotSignedInUserToSignInPage(String path) {
        var response = restTemplate.getForEntity(path, String.class);
        HTMLAssertions.assertSignInPage(response);
    }
}
