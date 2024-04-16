package com.binaryigor.htmxproductionsetup.day;

import com.binaryigor.htmxproductionsetup.shared.contracts.AuthUserApi;
import com.binaryigor.htmxproductionsetup.shared.views.HTMX;
import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

@RestController
public class DayController {

    private final AuthUserApi authUserApi;
    private final Clock clock;

    public DayController(AuthUserApi authUserApi,
                         Clock clock) {
        this.authUserApi = authUserApi;
        this.clock = clock;
    }

    @GetMapping("/day")
    String day() {
        var user = authUserApi.currentUserData();
        var day = """
                <h1>%s</h1>
                """.formatted(Translations.dayStart(user.name(), LocalDate.now(clock)));
        return HTMX.fragmentOrFullPage(day);
    }
}
