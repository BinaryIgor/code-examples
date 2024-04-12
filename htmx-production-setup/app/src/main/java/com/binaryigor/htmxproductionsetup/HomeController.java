package com.binaryigor.htmxproductionsetup;

import org.intellij.lang.annotations.Language;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
public class HomeController {

    @GetMapping("/home")
    String home() {
        @Language("HTML")
        var home = """
                <h1>Hello Igor</h1>
                <button class="rounded-md border-solid border-2 border-slate-300 py-2 px-8"
                    hx-get="/day" hx-replace-url=true hx-target="#app">Let's Go!</button>
                """;
        return HTMX.fragmentOrFullPage(home);
    }
}
