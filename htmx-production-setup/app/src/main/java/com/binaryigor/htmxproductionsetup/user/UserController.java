package com.binaryigor.htmxproductionsetup.user;

import com.binaryigor.htmxproductionsetup.shared.contracts.AuthUserClient;
import com.binaryigor.htmxproductionsetup.shared.views.HTMX;
import com.binaryigor.htmxproductionsetup.shared.views.Translations;
import com.binaryigor.htmxproductionsetup.shared.web.Cookies;
import com.binaryigor.htmxproductionsetup.user.domain.SignInRequest;
import com.binaryigor.htmxproductionsetup.user.domain.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

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
        var signInFormId = "sign-in-form";
        var signIn = """
                <h1 class='text-2xl font-bold'>%s</h1>
                <form-container id="%s"
                    form:hx-post='/sign-in'
                    form:hx-target='#app'
                    submit:add:class='button-like mt-4'
                    submit:value='%s'>
                    <input-with-error input:name="email">
                    </input-with-error>
                    <input-with-error input:type="password" input:name="password">
                    </input-with-error>
                </form-container>
                <script>
                    document.getElementById('%s').addEventListener('htmx:afterRequest',
                        function(e) {
                            const error = e.detail.xhr.response;
                            this.afterSubmit({ error: error });
                        });
                </script>
                """.formatted(Translations.signIn(), signInFormId, Translations.signIn(), signInFormId);
        return HTMX.fragmentOrFullPage(signIn);
    }

    @PostMapping(path = "/sign-in")
    String signIn(@ModelAttribute SignInRequest request,
                  HttpServletResponse response) {
        var signedInUser = userService.signIn(request);

        var token = signedInUser.authToken();

        response.addCookie(cookies.token(token.value(), token.expiresAt()));

        HTMX.addClientReplaceUrlHeader(response, "/home");

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
    public String home() {
        var user = userService.userOfId(authUserClient.currentId());
        return homePage(user.name());
    }

}
