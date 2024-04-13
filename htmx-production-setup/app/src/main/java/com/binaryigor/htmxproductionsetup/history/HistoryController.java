package com.binaryigor.htmxproductionsetup.history;

import com.binaryigor.htmxproductionsetup.shared.views.HTMX;
import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
public class HistoryController {

    //TODO: user scope
    @GetMapping("/history")
    String history() {
        var html = """
                <h1>%s</h1>
                """.formatted(Translations.history(Instant.now()));
        return HTMX.fragmentOrFullPage(html);
    }
}
