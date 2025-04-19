package com.binaryigor.httpserver.htmlexamples;

import com.binaryigor.httpserver.server.SimpleHttpServer;
import com.binaryigor.httpserver.utils.HttpResponses;

public class HtmlServer {
    public static void main(String[] args) {
        var server = new SimpleHttpServer(8080, 300_000);
        server.verbose(true);

        server.start(r -> {
            var subscribeForm = subscribeFormHTML();
            var subscribersList = subscribersListHTML();
            return HttpResponses.html(200, indexHTML(subscribeForm, subscribersList));
        });
    }

    private static String indexHTML(String subscribeForm, String subscribersList) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <title>Subscribe</title>
                </head>
                <body>
                    <h1>Subscribe</h1>
                    %s
                    %s
                </body>
                </html>
                """.formatted(subscribeForm, subscribersList);
    }

    private static String subscribeFormHTML() {
        return """
                <form style="padding: 4px" method="post" action="/subscribe" enctype="application/x-www-form-urlencoded">
                </form>""";
    }

    private static String subscribersListHTML() {
        return """
                <div>
                </div>
                """;
    }
}
