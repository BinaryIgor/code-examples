package com.binaryigor.htmxproductionsetup.day;

import com.binaryigor.htmxproductionsetup.shared.contracts.AuthUserApi;
import com.binaryigor.htmxproductionsetup.shared.exception.NotFoundException;
import com.binaryigor.htmxproductionsetup.shared.views.HTMX;
import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Collectors;

@RestController
public class DayController {

    private final DayRepository dayRepository;
    private final AuthUserApi authUserApi;
    private final Clock clock;

    public DayController(DayRepository dayRepository,
                         AuthUserApi authUserApi,
                         Clock clock) {
        this.dayRepository = dayRepository;
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

    @GetMapping("/history")
    String history() {
        var days = dayRepository.daysOfUser(authUserApi.currentId());

        var daysHtml = days.stream()
                .map(d ->
                        "<div class='rounded-md border-slate-300 border-2 px-8 py-2 cursor-pointer w-fit' "
                        + "hx-get='/history/%s' hx-push-url='true' hx-target='#app'>".formatted(d)
                        + "%s</div>".formatted(d))
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

    @GetMapping("/history/{day}")
    String historyDay(@PathVariable LocalDate day) {
        var dayFromDb = dayRepository.dayOfUser(authUserApi.currentId(), day)
                .orElseThrow(() -> new NotFoundException("Day"));

        var html = """
                <h2 class='text-xl mb-4'>%s</h2>
                <div class='mb-2'>%s:</div>
                <div>%s</div>
                """.formatted(Translations.historyOfDay(day),
                Translations.dayDescription(),
                dayFromDb.description());

        return HTMX.fragmentOrFullPage(html);
    }
}
