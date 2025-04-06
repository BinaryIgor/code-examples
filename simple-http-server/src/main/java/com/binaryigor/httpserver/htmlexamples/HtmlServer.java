package com.binaryigor.httpserver.htmlexamples;

import com.binaryigor.httpserver.server.HttpRequest;
import com.binaryigor.httpserver.server.HttpResponse;
import com.binaryigor.httpserver.server.SimpleHttpServer;
import com.binaryigor.httpserver.utils.HttpRequests;
import com.binaryigor.httpserver.utils.HttpResponses;

import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class HtmlServer {
    public static void main(String[] args) {
        var server = new SimpleHttpServer(8080, 300_000);
        server.verbose(true);

        var subscriberRepository = new SubscriberRepository();

        server.start(r -> {
            if (r.method().equals("POST")) {
                return handleNewSubscriber(r, subscriberRepository);
            }

            var subscribersList = subscriberRepository.all().stream()
                    .map(s -> "<div>%s:%s</div>".formatted(s.email(), s.name()))
                    .collect(Collectors.joining("\n"));

            var queryParams = HttpRequests.parseQueryParams(r.url(), StandardCharsets.UTF_8);
            var email = queryParams.getOrDefault("email", "");
            var emailError = Boolean.parseBoolean(queryParams.getOrDefault("emailError", "false"));
            var name = queryParams.getOrDefault("name", "");
            var nameError = Boolean.parseBoolean(queryParams.getOrDefault("nameError", "false"));

            var subscribers = """
                    <div style="padding-top: 8px">
                        <h2>They have subscribed already</h2>
                        %s
                    </div>
                    """.formatted(subscribersList);

            return HttpResponses.html(200,
                    indexHTML(subscribeFormHTML(email, emailError, name, nameError), subscribers));
        });
    }

    private static HttpResponse handleNewSubscriber(HttpRequest request, SubscriberRepository subscriberRepository) {
        var form = HttpRequests.parseUrlEncodedForm(request.body(), StandardCharsets.UTF_8);
        var email = form.get("email");
        var name = form.get("name");
        var emailError = email == null || email.isBlank();
        var nameError = name == null || name.isBlank();
        if (emailError || nameError) {
            return HttpResponses.redirect("/?emailError=%s&email=%s&nameError=%s&name=%s"
                    .formatted(emailError, email, nameError, name));
        }

        var subscriber = new Subscriber(email, name);
        subscriberRepository.save(subscriber);

        return HttpResponses.redirect("/");
    }

    private static String indexHTML(String subscribeForm, String subscribers) {
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
                """.formatted(subscribeForm, subscribers);
    }

    private static String subscribeFormHTML(String email, boolean emailError,
                                            String name, boolean nameError) {
        return """
                <form style="padding: 4px" method="post" action="/subscribe" enctype="application/x-www-form-urlencoded">
                    <input style="padding: 4px; display: block" type="email" name="email" placeholder="Email" value="%s">
                    <p style="color: red; padding: 0; margin-top: 0; margin-bottom: 8px; font-style: italic;%s">Email required</p>
                    <input style="padding: 4px; display: block" type="text" name="name" placeholder="Name" value="%s">
                    <p style="color: red; padding: 0; margin-top: 0; margin-bottom: 8px; font-style: italic;%s">Name required</p>
                    <input style="padding: 8px; margin-top: 8px" type="submit" value="Subscribe">
                </form>
                """.formatted(email, emailError ? "" : " display: none", name, nameError ? "" : " display: none");
    }
}
