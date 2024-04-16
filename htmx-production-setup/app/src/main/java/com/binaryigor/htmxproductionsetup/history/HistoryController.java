package com.binaryigor.htmxproductionsetup.history;

import com.binaryigor.htmxproductionsetup.shared.contracts.AuthUserApi;
import com.binaryigor.htmxproductionsetup.shared.views.HTMX;
import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Collectors;

@RestController
public class HistoryController {

    private final HistoryRepository historyRepository;
    private final AuthUserApi authUserApi;
    private final Clock clock;

    public HistoryController(HistoryRepository historyRepository,
                             AuthUserApi authUserApi,
                             Clock clock) {
        this.historyRepository = historyRepository;
        this.authUserApi = authUserApi;
        this.clock = clock;
    }

    @GetMapping("/history")
    String history() {
        var days = historyRepository.days(authUserApi.currentId());

        var daysHtml = days.stream()
                .map("<div class='rounded-md border-slate-300 border-2 px-8 py-2 cursor-pointer w-fit'>%s</div>"::formatted)
                .collect(Collectors.joining("\n"));

        var html = """
                <h1 class="text-xl bold mb-4">%s</h1>
                <div class='space-y-4'>
                %s
                </div>
                """.formatted(Translations.history(LocalDate.now(clock)),
                daysHtml);

        return HTMX.fragmentOrFullPage(html);
    }
}
