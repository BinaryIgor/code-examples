package com.binaryigor.htmxproductionsetup.shared.views;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class HTMX {

    // Defaults are for local development, when the app is running with dev Spring profile & start_tailwindcss_watch.bash script is on
    private static final String HTMX_PATH = System.getenv().getOrDefault("HTMX_PATH", "/lib/htmx.min.1.9.10.js");
    private static final String CSS_PATH = System.getenv().getOrDefault("CSS_PATH", "/live-styles.css");
    private static final String INDEX_JS_PATH = System.getenv().getOrDefault("INDEX_JS_PATH", "/index.js");
    private static final String COMPONENTS_PATH = System.getenv().getOrDefault("COMPONENTS_PATH", "/components.js");
    public static String HTMX_REQUEST_HEADER = "hx-request";

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
        if (isHTMXRequest()) {
            return fragment;
        }
        return fullPage(fragment, hiddenNavigation);
    }

    public static boolean isHTMXRequest() {
        var currentRequest = currentRequest();
        return currentRequest.isPresent() && isHTMXRequest(currentRequest.get());
    }

    public static boolean isHTMXRequest(HttpServletRequest request) {
        return request.getHeader(HTMX_REQUEST_HEADER) != null;
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
                       
                <body class='bg-slate-50 text-slate-700'>
                    %s
                    <info-modal id="error-modal" title="%s" title:add:class="text-red-500"></info-modal>
                    <div hx-history="false" hx-history-elt id="app" class="p-4">
                         %s
                    </div>
                </body>
                       
                </html>""".formatted(Translations.appTitle(), CSS_PATH, INDEX_JS_PATH, COMPONENTS_PATH, HTMX_PATH,
                navigationComponent(hiddenNavigation), Translations.errorModalTitle(), fragment).strip();
    }

    private static String navigationComponent(boolean hidden) {
        return """
                <div id="app-navigation"
                    class="%sz-10 sticky flex justify-between top-0 w-full py-4 px-2 border-b-2 border-slate-300">
                    <div class="text-center text-xl cursor-pointer italic"
                        hx-get="/"
                        onclick="pushHomeIfNotAtHome(this)"
                        hx-trigger="render-home"
                        hx-target="#app">%s</div>
                    <div class="cursor-pointer text-lg text-right relative w-fit"
                      hx-post="/sign-out"
                      hx-trigger="click"
                      hx-replace-url="true"
                      hx-swap="innerHTML"
                      hx-target="#app">%s</div>
                </div>
                """.formatted(hidden ? "hidden " : "", Translations.appTitle(), Translations.signOut());
    }

    public static void addClientReplaceUrlHeader(HttpServletResponse response, String url) {
        response.addHeader("hx-replace-url", url);
    }

    public static void addTriggerHeader(HttpServletResponse response, String trigger) {
        response.addHeader("hx-trigger", trigger);
    }

    public static String inlineScript(String script) {
        return """
                <script>
                (function(){
                    %s
                })();
                </script>""".formatted(script);
    }
}
