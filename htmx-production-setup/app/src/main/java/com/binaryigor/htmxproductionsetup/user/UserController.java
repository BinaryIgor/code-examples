package com.binaryigor.htmxproductionsetup.user;

import com.binaryigor.htmxproductionsetup.shared.contracts.AuthUserClient;
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
    private final AuthUserClient authUserClient;

    public UserController(UserService userService,
                          Cookies cookies,
                          AuthUserClient authUserClient) {
        this.userService = userService;
        this.cookies = cookies;
        this.authUserClient = authUserClient;
    }

    @GetMapping("/sign-in")
    String signInPage() {
        var signIn = """
                <h1 class='text-2xl font-bold'>%s</h1>
                <form-container id='sign-in-form'
                    form:hx-post='/sign-in'
                    form:hx-target='#app'
                    submit:add:class='button-like mt-4'
                    submit:value='%s'>
                    %s
                    %s
                </form-container>
                <script>
                    const formContainer = document.getElementById('sign-in-form');
                    formContainer.addEventListener('htmx:afterRequest',
                        function(e) {
                            const error = e.detail.xhr.response;
                            this.afterSubmit({ error: error });
                        });
                </script>
                """.formatted(Translations.signIn(), Translations.signIn(),
                inputWithError("email-input", "email", "text", "/sign-in/validate-email"),
                inputWithError("password-input", "password", "password", "/sign-in/validate-password"));
        return HTMX.fragmentOrFullPage(signIn, true);
    }

    private String inputWithError(String id, String name, String type, String validateEndpoint) {
        return """
                <input-with-error id="%s" input:name="%s" input:type="%s"
                    input:hx-trigger='input changed delay:500ms'
                    input:hx-post='%s'
                    input:hx-swap="outerHTML"
                    input:hx-target="next input-error">
                </input-with-error>""".formatted(id, name, type, validateEndpoint);
    }

    @PostMapping("/sign-in/validate-email")
    String signInValidateEmail(@RequestParam String email) {
        var error = Translations.exception(() -> userService.validateEmail(email));
        return Views.inputError(error);
    }

    @PostMapping("/sign-in/validate-password")
    String signInValidatePassword(@RequestParam String password) {
        var error = Translations.exception(() -> userService.validatePassword(password));
        return Views.inputError(error);
    }

    @PostMapping("/sign-in")
    String signIn(@ModelAttribute SignInRequest request,
                  HttpServletResponse response) {
        var signedInUser = userService.signIn(request);

        var token = signedInUser.authToken();

        response.addCookie(cookies.token(token.value(), token.expiresAt()));

        HTMX.addClientReplaceUrlHeader(response, "/home");
        HTMX.addTriggerHeader(response, "top-navigation-show");

        return HTMX.fragmentOrFullPage(homePage(signedInUser.name()));
    }

    private String homePage(String userName) {
        var home = """
                <h1 class="mb-4 text-xl">%s</h1>
                <div class="space-y-4">
                    <button class="button-like block"
                        hx-get="/day" hx-push-url=true hx-target="#app">%s</button>
                    <button class="button-like"
                        hx-get="/history" hx-push-url=true hx-target="#app">%s</button>
                </div>
                """.formatted(
                Translations.hello(userName),
                Translations.homeStart(),
                Translations.homeHistory());
        return HTMX.fragmentOrFullPage(home);
    }

    @GetMapping("/home")
    String home() {
        var user = userService.userOfId(authUserClient.currentId());
        return homePage(user.name());
    }

    @PostMapping("/sign-out")
    String signOut(HttpServletRequest request,
                   HttpServletResponse response) {
        cookies.tokenValue(request.getCookies())
                .ifPresent(t -> response.addCookie(cookies.expiredToken()));

        HTMX.addClientReplaceUrlHeader(response, "/sign-in");
        HTMX.addTriggerHeader(response, "top-navigation-hide");

        return signInPage();
    }
}