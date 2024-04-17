package com.binaryigor.htmxproductionsetup.user;

import com.binaryigor.htmxproductionsetup.HTMLAssertions;
import com.binaryigor.htmxproductionsetup.IntegrationTest;
import com.binaryigor.htmxproductionsetup.shared.Language;
import com.binaryigor.htmxproductionsetup.shared.contracts.UserData;
import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserControllerTest extends IntegrationTest {

    @BeforeEach
    void setup() {
        Translations.setCurrentLanguage(Language.EN);
    }

    @Test
    void returnsSignInPage() {
        var response = restTemplate.getForEntity("/sign-in", String.class);
        HTMLAssertions.assertSignInPage(response);
    }

    @Test
    void doesNotSignInNonexistentUser() {
        var response = userTestFixture.signIn("email@emial.com", "ComplexPassword12");

        Assertions.assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(response.getBody())
                .isEqualTo(Translations.notFoundException(Language.EN, "User"));
    }

    @Test
    void signsInExistingUser() {
        var user = new UserData(UUID.randomUUID(), "email@gmail.com",
                "some-name", Language.EN);
        var password = "ComplexPassword12";
    }
}
