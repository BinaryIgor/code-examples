package com.binaryigor.htmxproductionsetup.shared.views;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class HTMX {

    private static final String APP_TITLE = "HTMX Production Setup";
    //TODO: prod/non-prod distinction
    private static final String HTMX_PATH = System.getenv().getOrDefault("HTMX_PATH", "lib/htmx.min.1.9.10.js");
    private static final String CSS_PATH = System.getenv().getOrDefault("CSS_PATH", "live-styles.css");
    private static final String INDEX_JS_PATH = System.getenv().getOrDefault("INDEX_JS_PATH", "index.js");
    private static final String COMPONENTS_PATH = System.getenv().getOrDefault("COMPONENTS_PATH", "components.js");

    private static Optional<HttpServletRequest> currentRequest() {
        var ra = RequestContextHolder.getRequestAttributes();
        if (ra instanceof ServletRequestAttributes sra) {
            return Optional.of(sra.getRequest());
        }
        return Optional.empty();
    }

    public static String fragmentOrFullPage(String fragment) {
        return fragmentOrFullPage(fragment, false);
    }

    public static String fragmentOrFullPage(String fragment, boolean hiddenNavigation) {
        var currentRequest = currentRequest();
        if (currentRequest.isEmpty() || !isHTMXRequest(currentRequest.get())) {
            return fullPage(fragment, hiddenNavigation);
        }
        return fragment;
    }

    public static boolean isHTMXRequest(HttpServletRequest request) {
        return request.getHeader("hx-request") != null;
    }

    public static String fullPage(String fragment, boolean hiddenNavigation) {
        return """
                <!DOCTYPE HTML>
                <html lang="en">
                <head>
                    <title>%s</title>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <link rel="stylesheet" href="%s" />
                    <script defer src="%s"></script>
                    <script type="module" src="%s"></script>
                    <script src="%s"></script>
                </head>
                       
                <body>
                    %s
                    <info-modal id="error-modal" title="Something went wrong..." title:add:class="text-red-500"></info-modal>
                    <div hx-history="false" hx-history-elt id="app" class="p-4">
                         %s
                    </div>
                </body>
                       
                </html>""".formatted(APP_TITLE, CSS_PATH, INDEX_JS_PATH, COMPONENTS_PATH, HTMX_PATH,
                navigationComponent(hiddenNavigation), fragment).strip();
    }

    private static String navigationComponent(boolean hidden) {
        return """
                <div id="app-navigation"
                    class="%sz-10 sticky flex justify-between top-0 w-full py-4 px-2 border-b-4">
                    <div class="text-center text-xl">%s</div>
                    <div class="cursor-pointer text-lg text-right relative w-fit"
                      hx-post="/sign-out"
                      hx-trigger="click"
                      hx-replace-url="true"
                      hx-swap="innerHTML"
                      hx-target="#app">%s</div>
                </div>
                """.formatted(hidden ? "hidden " : "", APP_TITLE, Translations.signOut());
    }

    public static void addClientReplaceUrlHeader(HttpServletResponse response, String url) {
        response.addHeader("hx-replace-url", url);
    }

    public static void addTriggerHeader(HttpServletResponse response, String trigger) {
        response.addHeader("hx-trigger", trigger);
    }
}