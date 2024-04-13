package com.binaryigor.htmxproductionsetup.day;

import com.binaryigor.htmxproductionsetup.shared.views.HTMX;
import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class DayController {

    //TODO: user scope
    @GetMapping("/day")
    String day() {
        var day = """
                <h1>%s</h1>
                """.formatted(Translations.day(Instant.now()));
        return HTMX.fragmentOrFullPage(day);
    }
}
