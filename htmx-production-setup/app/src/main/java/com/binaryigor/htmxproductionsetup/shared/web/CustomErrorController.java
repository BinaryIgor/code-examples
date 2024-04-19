package com.binaryigor.htmxproductionsetup.shared.web;

import com.binaryigor.htmxproductionsetup.shared.views.HTMX;
import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
public class CustomErrorController implements ErrorController {

    public static String notFoundErrorPage(String notFoundMessage) {
        return HTMX.fullPage("""
                        <h1 class="text-xl font-bold mb-4">%s</h1>
                        <div class="mb-4">%s</div>
                        <div>%s<div>
                        """.formatted(Translations.notFoundTitle(),
                        notFoundMessage,
                        Translations.backToTheMainPage()),
                true);
    }

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        var status = Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
                .orElse(500);

        var statusCode = Integer.parseInt(status.toString());
        if (statusCode == HttpStatus.NOT_FOUND.value()) {
            var notFoundPath = (String) request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);
            return notFoundErrorPage(Translations.notFoundMessage(notFoundPath));
        }

        return HTMX.fullPage("""
                        <h1 class="text-xl font-bold mb-4">%s</h1>
                        %s
                        """.formatted(Translations.unknownErrorTitle(),
                        Translations.backToTheMainPage()),
                true);
    }
}
