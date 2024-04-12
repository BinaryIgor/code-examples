package com.binaryigor.htmxproductionsetup;

import com.binaryigor.htmxproductionsetup.shared.views.HTMX;
import org.intellij.lang.annotations.Language;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class DayController {

    //TODO: user scope
    @GetMapping("/day")
    String day() {
        @Language("HTML")
        var day = """
                <h1>Let's start the day %s!</h1>
                """.formatted(Instant.now());
        return HTMX.fragmentOrFullPage(day);
    }
}
