package com.binaryigor.htmxproductionsetup.day;

import com.binaryigor.htmxproductionsetup.day.domain.Day;
import com.binaryigor.htmxproductionsetup.day.domain.DayService;
import com.binaryigor.htmxproductionsetup.day.domain.SaveCurrentDayRequest;
import com.binaryigor.htmxproductionsetup.shared.contracts.AuthUserApi;
import com.binaryigor.htmxproductionsetup.shared.views.HTMX;
import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.stream.Collectors;

@RestController
public class DayController {

    private final DayService dayService;
    private final AuthUserApi authUserApi;
    private final Clock clock;

    public DayController(DayService dayService,
                         AuthUserApi authUserApi,
                         Clock clock) {
        this.dayService = dayService;
        this.authUserApi = authUserApi;
        this.clock = clock;
    }

    @GetMapping("/day")
    String day() {
        var currentDay = dayService.currentDayOfUser(authUserApi.currentId());
        var dayNote = currentDay.map(Day::note).orElse("");

        var day = """
                <h1 class="text-xl my-4">%s</h1>
                <form id='day-form' hx-post="/day" hx-swap=none>
                    <span id="note-changes-span" class="opacity-80 italic hidden"
                        data-message-not-saved-changes='%s'
                        data-message-saved-changes='%s'></span>
                    <textarea id='note-textarea'
                        class='bg-white rounded p-2 border-2 border-solid border-slate-200 focus:border-slate-300 outline-none
                        h-24 w-full max-w-3xl resize-none block'
                        name='note'
                        placeholder='%s'>%s</textarea>
                    <input type="submit" class='button-like mt-4' value="%s">
                <form>""".formatted(Translations.dayStart(LocalDate.now(clock)),
                Translations.notSavedDayChanges(),
                Translations.savedDayChanges(),
                Translations.dayNotePlaceholder(),
                dayNote,
                Translations.saveDay());

        var script = HTMX.inlineScript("""
                const hiddenClass = 'hidden';
                                
                const noteTextarea = document.getElementById('note-textarea');
                const initialNote = noteTextarea.value;
                                
                const noteChangesSpan = document.getElementById('note-changes-span');
                const notSavedChangesMessage = noteChangesSpan.getAttribute('data-message-not-saved-changes');
                const savedChangesMessage = noteChangesSpan.getAttribute('data-message-saved-changes');
                                
                noteTextarea.addEventListener("input", e => {
                    if (noteTextarea.value == initialNote) {
                        noteChangesSpan.classList.add(hiddenClass);
                    } else {
                        noteChangesSpan.textContent = notSavedChangesMessage;
                        noteChangesSpan.classList.remove(hiddenClass);
                    }
                });
                                
                document.getElementById('day-form').addEventListener('htmx:afterRequest', e => {
                    if (e.detail.successful) {
                       noteChangesSpan.textContent = savedChangesMessage;
                       noteChangesSpan.classList.remove(hiddenClass);
                    }
                });
                """);

        return HTMX.fragmentOrFullPage(day + "\n" + script);
    }

    @PostMapping("/day")
    void saveCurrentDay(@ModelAttribute SaveCurrentDayRequest request) {
        dayService.saveCurrentDay(authUserApi.currentId(), request.note());
    }

    @GetMapping("/history")
    String history() {
        var daysHtml = dayService.daysOfUser(authUserApi.currentId()).stream()
                .map(d ->
                        "<div class='button-like w-fit' "
                        + "hx-get='/history/%s' hx-push-url='true' hx-target='#app'>".formatted(d)
                        + "%s</div>".formatted(d))
                .collect(Collectors.joining("\n"));

        var html = """
                <h1 class="text-xl my-4">%s</h1>
                <div class='space-y-4'>
                %s
                </div>
                """.formatted(Translations.history(LocalDate.now(clock)),
                daysHtml);

        return HTMX.fragmentOrFullPage(html);
    }

    @GetMapping("/history/{day}")
    String historyDay(@PathVariable LocalDate day) {
        var dayFromDb = dayService.historicalDayOfUser(authUserApi.currentId(), day);

        var html = """
                <h1 class='text-xl mb-4'>%s</h1>
                <div class='mb-2 font-bold'>%s:</div>
                <div class='italic'>%s</div>
                """.formatted(Translations.historyOfDay(day),
                Translations.dayNote(),
                dayFromDb.note());

        return HTMX.fragmentOrFullPage(html);
    }
}
