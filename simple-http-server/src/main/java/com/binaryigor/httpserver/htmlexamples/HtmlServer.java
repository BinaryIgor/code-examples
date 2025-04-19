package com.binaryigor.httpserver.htmlexamples;

import com.binaryigor.httpserver.server.HttpRequest;
import com.binaryigor.httpserver.server.HttpResponse;
import com.binaryigor.httpserver.server.SimpleHttpServer;
import com.binaryigor.httpserver.utils.HttpRequests;
import com.binaryigor.httpserver.utils.HttpResponses;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

public class HtmlServer {
    public static void main(String[] args) {
        var server = new SimpleHttpServer(8080, 300_000);
        server.verbose(true);

        var subscriberRepository = new SubscriberRepository();

        server.start(r -> {
            if (r.method().equals("POST") && r.url().contains("/subscribe")) {
                return handleSubscribe(r, subscriberRepository);
            }

            var queryParams = HttpRequests.parseQueryParams(r.url(), StandardCharsets.UTF_8);
            var emailError = Boolean.parseBoolean(queryParams.getOrDefault("emailError", "false"));
            var email = queryParams.getOrDefault("email", "");
            var nameError = Boolean.parseBoolean(queryParams.getOrDefault("nameError", "false"));
            var name = queryParams.getOrDefault("name", "");

            var subscribeForm = subscribeFormHTML(email, emailError, name, nameError);
            var subscribersList = subscribersListHTML(subscriberRepository.all());
            return HttpResponses.html(200, indexHTML(subscribeForm, subscribersList));
        });
    }

    private static HttpResponse handleSubscribe(HttpRequest request, SubscriberRepository subscriberRepository) {
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

    private static String subscribeFormHTML(String email, boolean emailError,
                                            String name, boolean nameError) {
        return """
                <form style="padding: 4px" method="post" action="/subscribe" enctype="application/x-www-form-urlencoded">
                <input style="display: block" type="email" name="email" placeholder="Email" value="%s">
                <p style="margin-top: 2px; margin-bottom: 8px; font-style: italic; color: red;%s">Email required</p>
                <input style="display: block" type="text" name="name" placeholder="Name" value="%s">
                <p style="margin-top: 2px; margin-bottom: 8px; font-style: italic; color: red;%s">Name required</p>
                <input type="submit" value="Subscribe">
                </form>""".formatted(
                email, emailError ? "" : "display: none",
                name, nameError ? "" : "display: none");
    }

    private static String subscribersListHTML(List<Subscriber> subscribers) {
        var subscribersHTML = subscribers.stream()
                .map(s -> "<div>%s:%s</div>".formatted(s.email(), s.name()))
                .collect(Collectors.joining("\n"));
        return """
                <div>
                    <h2>Already subscribed</h2>
                    %s
                </div>
                """.formatted(subscribersHTML);
    }
}
