package com.binaryigor.htmxproductionsetup.user;

import com.binaryigor.htmxproductionsetup.shared.contracts.AuthUserApi;
import com.binaryigor.htmxproductionsetup.shared.views.HTMX;
import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import com.binaryigor.htmxproductionsetup.shared.views.Views;
import com.binaryigor.htmxproductionsetup.shared.web.Cookies;
import com.binaryigor.htmxproductionsetup.user.domain.SignInRequest;
import com.binaryigor.htmxproductionsetup.user.domain.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    private final UserService userService;
    private final Cookies cookies;
    private final AuthUserApi authUserApi;

    public UserController(UserService userService,
                          Cookies cookies,
                          AuthUserApi authUserApi) {
        this.userService = userService;
        this.cookies = cookies;
        this.authUserApi = authUserApi;
    }

    @GetMapping("/sign-in")
    String signInPage() {
        var html = """
                <h1 class='text-3xl font-bold mb-4'>%s</h1>
                <form-container id='sign-in-form'
                    form:add:class="max-w-80"
                    form:hx-post='/sign-in'
                    form:hx-target='#app'
                    submit:add:class='button-like mt-4 w-full'
                    submit:value='%s'>
                    %s
                    %s
                </form-container>""".formatted(Translations.signIn(), Translations.signIn(),
                inputWithError("email-input", "email", "text", Translations.emailPlaceholder(),
                        "/sign-in/validate-email"),
                inputWithError("password-input", "password", "password", Translations.passwordPlaceholder(),
                        "/sign-in/validate-password"));

        var script = HTMX.inlineScript("""
                document.getElementById('sign-in-form').addEventListener('htmx:afterRequest',
                        function(e) {
                            const error = e.detail.xhr.response;
                            this.afterSubmit({ error: error });
                        });
                """);

        return HTMX.fragmentOrFullPage(html + "\n" + script, true);
    }

    private String inputWithError(String id,
                                  String name,
                                  String type,
                                  String placeholder,
                                  String validateEndpoint) {
        return """
                <input-with-error id="%s" input:name="%s" input:type="%s" input:placeholder="%s"
                    input:add:class="mt-2 w-full"
                    input:hx-trigger='input changed delay:500ms'
                    input:hx-post='%s'
                    input:hx-swap="outerHTML"
                    input:hx-target="next input-error">
                </input-with-error>""".formatted(id, name, type, placeholder, validateEndpoint);
    }

    @PostMapping("/sign-in/validate-email")
    String signInValidateEmail(@RequestParam String email) {
        var error = Translations.exceptionIfThrown(() -> userService.validateEmail(email));
        return Views.inputError(error);
    }

    @PostMapping("/sign-in/validate-password")
    String signInValidatePassword(@RequestParam String password) {
        var error = Translations.exceptionIfThrown(() -> userService.validatePassword(password));
        return Views.inputError(error);
    }

    @PostMapping("/sign-in")
    String signIn(@ModelAttribute SignInRequest request, HttpServletResponse response) {
        var signedInUser = userService.signIn(request);

        var token = signedInUser.authToken();

        response.addCookie(cookies.token(token.value(), token.expiresAt()));

        HTMX.addClientReplaceUrlHeader(response, "/");
        HTMX.addTriggerHeader(response, "top-navigation-show");

        return HTMX.fragmentOrFullPage(homePage(signedInUser.name()));
    }

    private String homePage(String userName) {
        var html = """
                <h1 class="my-8 text-2xl">%s</h1>
                <div class="space-y-4 max-w-80">
                    <button class="button-like w-full block"
                        hx-get="/day" hx-push-url=true hx-target="#app">%s</button>
                    <button class="button-like w-full"
                        hx-get="/history" hx-push-url=true hx-target="#app">%s</button>
                </div>
                """.formatted(
                Translations.hello(userName),
                Translations.homeToday(),
                Translations.homeHistory());
        return HTMX.fragmentOrFullPage(html);
    }

    @GetMapping("/")
    String home() {
        var user = authUserApi.currentUser();
        return homePage(user.name());
    }

    @PostMapping("/sign-out")
    String signOut(HttpServletRequest request, HttpServletResponse response) {
        cookies.tokenValue(request.getCookies())
                .ifPresent(t -> response.addCookie(cookies.expiredToken()));

        HTMX.addClientReplaceUrlHeader(response, "/sign-in");
        HTMX.addTriggerHeader(response, "top-navigation-hide");

        return signInPage();
    }
}
