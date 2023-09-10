import { Translations } from "../shared/translations";
import * as Views from "../shared/views";

export const SIGN_FORM_LABEL = "sign-in-form";

export const CLASSES = {
    signInNameInputError: "mb-4",
    signInPasswordInputError: "mb-4"
};

export function signInPage(signInEndpoint: string,
    validateNameEndpoint: string,
    validatePasswordEndpoint: string,
    withNavigationToHide: boolean,
    renderFullPage: boolean): string {

    const nameInput = Views.inputWithHiddenError({
        name: "name",
        placeholder: Translations.defaultLocale.signInPage.namePlaceholder,
        validateEndpoint: validateNameEndpoint,
        errorClasses: CLASSES.signInNameInputError
    });

    const passwordInput = Views.inputWithHiddenError({
        name: "password",
        placeholder: Translations.defaultLocale.signInPage.passwordPlaceholder,
        validateEndpoint: validatePasswordEndpoint,
        type: "password",
        errorClasses: CLASSES.signInPasswordInputError
    })

    const page = `${withNavigationToHide ? Views.navigationComponent(null, true): ""}
    <h1 class="p-4 text-2xl">Let's get some wisdom</h1>
    <form class="p-4 relative w-fit"
        hx-post="${signInEndpoint}"
        hx-target="#${Views.ROOT_ID}"
        hx-replace-url="/">
        ${nameInput}
        ${passwordInput}
        <input class="w-full py-4 ${Views.DISABLED_CLASS} ${Views.BUTTON_LIKE_CLASSES}" 
            type="submit" value="${Translations.defaultLocale.signInPage.signInButton}"
        ${Views.SUBMIT_FORM_LABEL}="${SIGN_FORM_LABEL}" ${Views.DISABLED_CLASS}>
    </form>`.trim();
    return renderFullPage ? Views.wrappedInMainPage(page, null) : page;
}

export function profilePage(currentUser: string,  renderFullPage: boolean): string {
    const page =  `<div class="p-4">TODO: profile page for ${currentUser} user</div>`;
    return renderFullPage ? Views.wrappedInMainPage(page, currentUser): page;
}