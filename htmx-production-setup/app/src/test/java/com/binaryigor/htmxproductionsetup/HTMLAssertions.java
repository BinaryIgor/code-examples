package com.binaryigor.htmxproductionsetup;

import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import org.assertj.core.api.Assertions;
import org.jsoup.Jsoup;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class HTMLAssertions {

    public static void assertSignInPage(ResponseEntity<String> response) {
        Assertions.assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.OK);

        var html = Jsoup.parse(response.getBody());
        Assertions.assertThat(html.getElementsByTag("h1").getFirst().text())
                .isEqualTo(Translations.signIn());
    }
}
