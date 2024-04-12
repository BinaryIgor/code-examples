package com.binaryigor.htmxproductionsetup;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

public class HTMX {

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
        var currentRequest = currentRequest();
        if (currentRequest.isEmpty() || !isHTMXRequest(currentRequest.get())) {
            return fullPage(fragment);
        }
        return fragment;
    }

    public static boolean isHTMXRequest(HttpServletRequest request) {
        return request.getHeader("hx-request") != null;
    }

    public static String fullPage(String fragment) {
        return """
                <!DOCTYPE HTML>
                <html lang="en">
                <head>
                    <title>HTMX Production Setup</title>
                    <meta charset="UTF-8" />
                    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                    <link rel="stylesheet" href="%s" />
                    <script defer src="%s"></script>
                    <script type="module" src="%s"></script>
                    <script src="%s"></script>
                </head>
                       
                <body>
                    <info-modal id="error-modal" title="Something went wrong..." title:add:class="text-red-500"></info-modal>
                    <div hx-history="false" id="app" class="p-4">
                         %s
                    </div>
                </body>
                       
                </html>""".formatted(CSS_PATH, INDEX_JS_PATH, COMPONENTS_PATH, HTMX_PATH, fragment).strip();
    }
}
