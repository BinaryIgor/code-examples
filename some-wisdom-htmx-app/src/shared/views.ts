import { getAssetsSrc } from "../app-config";
import { ErrorCode, OptionalErrorCode } from "./errors";
import { Translations } from "./translations";

export const ROOT_ID = "app";

export const AUTHORS_SEARCH_INPUT = "authors-search";

export const FORM_LABEL = "data-form";
export const CONFIRMABLE_ELEMENT_TITLE_LABEL = "data-confirmable-element-title";
export const CONFIRMABLE_ELEMENT_CONTENT_LABEL = "data-confirmable-element-content";
export const SUBMIT_FORM_LABEL = "data-submit-form";

export const HIDDEN_CLASS = "hidden";
export const DISABLED_CLASS = "disabled";

export const TRIGGERS = {
    changeRoute: "change-route",
    resetScroll: "reset-scroll"
};

const SIGN_IN_ENDPOINT = "/user/sign-in";
const SIGN_OUT_ENDPOINT = "/user/sign-out";
const PROFILE_ENDPOINT = "/user/profile";

export const PROPS = {
    bgColorPrimary: "bg-indigo-950",
    bgColorSecondary1: "bg-indigo-900",
    bgColorSecondary2: "bg-indigo-800",
    txtColorPrimary: "text-zinc-200",
    txtColorSecondary1: "text-zinc-300",
    txtColorSecondary2: "text-zinc-400",
    txtColorBtn: "text-zinc-100",
    bgColorBtn: "bg-indigo-600",
    hoverTxtColorBtn: "hover:text-zinc-300",
    hoverBgColorBtn: "hover:bg-indigo-700",
    borderColorPrimary: "border-indigo-950",
    borderColorSecondary1: "border-indigo-900",
    borderColorSecondary2: "border-indigo-800",
    placeholderColor: "placeholder-zinc-500",
    hoverBgColorPrimary: "hover:bg-indigo-950",
    hoverBgColorSecondary1: "hover:bg-indigo-900",
    hoverBgColorSecondary2: "hover:bg-indigo-800",
    hoverTxtColorPrimary: "hover:text-zinc-200",
    hoverTxtColorSecondary1: "hover:text-zinc-300",
    hoverTxtColorSecondary2: "hover:text-zinc-400",
    shadowColorSecondary1: "shadow-indigo-900",
    shadowColorSecondary2: "shadow-indigo-800",
};

export const BUTTON_LIKE_CLASSES = `rounded-lg ${PROPS.bgColorBtn} ${PROPS.txtColorBtn} 
    ${PROPS.hoverBgColorBtn} ${PROPS.hoverTxtColorBtn}`;

export const INPUT_LIKE_CLASSES = `p-2 rounded-md ${PROPS.bgColorSecondary1} shadow-md mb-2 
    border-2 ${PROPS.borderColorSecondary2} 
    ${PROPS.hoverBgColorSecondary2} focus:outline-none
    ${PROPS.placeholderColor}`;

const ERROR_MESSAGE_CLASS = "error-message";
const HX_ERROR_MESSAGE_TARGET = `next .${ERROR_MESSAGE_CLASS}`;

export const CLOSE_ICON = "&times;";

const MODAL_CLASSES = "fixed w-full h-full z-50 pt-32 bg-black/60";
const MODAL_CONTENT_CLASSES = `w-11/12 md:w-8/12 xl:w-6/12 p-4 m-auto ${PROPS.bgColorSecondary1}
    relative rounded-lg`;

export function wrappedInMainPage(html: string, currentUser: string | null): string {
    const assetsSrc = getAssetsSrc();
    return `<!DOCTYPE html>
    <html lang="en">
      <head>
        <meta charset="UTF-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    
        <title>${Translations.defaultLocale.appTitle}</title>
        <link rel="stylesheet" href="${assetsSrc.styles}"/>
      </head>
      <body class="${PROPS.bgColorPrimary} ${PROPS.txtColorPrimary}">
        ${confirmableModal()}
        ${errorModal()}
        ${navigationComponent(currentUser, false)}
        
        <div hx-history="false" id="${ROOT_ID}" hx-history-elt>
            ${html}
        </div>
      </body>

      <script src="${assetsSrc.htmx}"></script>
      <script src="${assetsSrc.indexJs}"></script>
    </html>`;
}

export function wrappedInCenteredDiv(html: string): string {
    return `<div class="lg:px-40 xl:px-60 2xl:px-80">${html.trim()}</div>`
}

export function wrappedInToLeftDiv(html: string): string {
    return `<div class="lg:pr-60 xl:pr-80 2xl:pr-[30rem]">${html.trim()}</div>`
}

export function inputWithHiddenError(props: {
    name: string,
    placeholder: string,
    validateEndpoint: string,
    type?: string,
    inputClasess?: string,
    errorClasses?: string
}): string {

    let inputClasess = INPUT_LIKE_CLASSES;

    if (props.inputClasess) {
        inputClasess += ` ${props.inputClasess}`
    }

    return `<input name="${props.name}" placeholder="${props.placeholder}" type="${props.type ?? 'text'}" 
        class="${inputClasess}"
        hx-trigger="input delay:500ms"
        hx-target="${HX_ERROR_MESSAGE_TARGET}"
        hx-swap="outerHTML"
        hx-post="${props.validateEndpoint}">
    ${inputErrorIf(null, props.errorClasses)}`
}

export function textAreaWithHiddenError(name: string, placeholder: string, validateEndpoint: string,
    textAreaClasess?: string): string {
    let actualTextAreaClasses = `h-24 w-full resize-none ${INPUT_LIKE_CLASSES}`;

    if (textAreaClasess) {
        actualTextAreaClasses += ` ${textAreaClasess}`;
    }

    return `<textarea name="${name}" placeholder="${placeholder}" class="${actualTextAreaClasses}"
        hx-trigger="keyup changed delay:500ms"
        hx-target="${HX_ERROR_MESSAGE_TARGET}"
        hx-swap="outerHTML"
        hx-post="${validateEndpoint}"></textarea>
    ${inputErrorIf()}`
}

export function errorsComponent(errors: ErrorCode[]): string {
    const errorsHtml = errors.map(e => `<p class="text-red-600 text-lg">${translatedError(e)}</p>`).join('\n');
    return `<div class="space-y-2 pb-4">${errorsHtml}</div>`;
}

function translatedError(error: ErrorCode): string {
    const errorsTranslations = Translations.defaultLocale.errors as any;
    return errorsTranslations[error] ?? error;
}

export function inputErrorIf(error: OptionalErrorCode = null, additionalClasses?: string): string {
    const translated = error ? translatedError(error) : "";
    let classes = `${ERROR_MESSAGE_CLASS} ${translated ? 'active' : 'inactive'}`;
    if (additionalClasses) {
        classes += ` ${additionalClasses}`;
    }
    return `<p class="${classes}">${translated}</p>`
}

export function inlineJs(js: string, scoped: boolean = true): string {
    if (scoped) {
        js = `(function() { ${js} }());`;
    }
    return `<script>${js}</script>`
}

function confirmableModal(): string {
    return `<div class="${HIDDEN_CLASS} ${MODAL_CLASSES}" id="confirmable-modal">
        <div class="${MODAL_CONTENT_CLASSES}">
            ${closeModalIconElement("confirmable-modal-close")}
            <div class="text-2xl font-medium" id="confirmable-modal-title">Confirm</div>
            <div class="text-lg pt-4 pb-12" id="confirmable-modal-content"></div>
            <span id="confirmable-modal-cancel" 
                class="absolute bottom-0 left-0 p-4 cursor-pointer font-medium text-xl">
                    ${Translations.defaultLocale.confirmableModal.cancel}</span>
            <span id="confirmable-modal-ok" 
                class="absolute bottom-0 right-0 p-4 cursor-pointer font-medium text-xl">
                ${Translations.defaultLocale.confirmableModal.ok}</span>
        </div>
    </div>`;
}

function closeModalIconElement(id: string): string {
    return `<span id="${id}" class="text-4xl absolute top-0 right-2
        ${PROPS.hoverTxtColorSecondary2} cursor-pointer">${CLOSE_ICON}</span>`;
}

export function errorModal(): string {
    return `<div class="${HIDDEN_CLASS} ${MODAL_CLASSES}" id="error-modal">
        <div class="${MODAL_CONTENT_CLASSES}">
            ${closeModalIconElement("error-modal-close")}
            <div id="error-modal-content"></div>
        </div>
    </div>`;
}

export function navigationComponent(currentUser: string | null, withExternalSwap: boolean): string {
    let navigationClasses = `z-10 sticky flex justify-between top-0 w-full py-4 px-2 border-b-4  
        ${PROPS.borderColorSecondary2} ${PROPS.bgColorPrimary} ${PROPS.txtColorSecondary2}`;

    if (!currentUser) {
        navigationClasses += ` ${HIDDEN_CLASS}`;
    }

    const dropDownElementClasses = `${PROPS.hoverBgColorPrimary} ${PROPS.hoverTxtColorPrimary} py-2 px-4`;

    //Id is used by index.js
    return `<div id="app-navigation" class="${navigationClasses}" ${oobSwapIf(withExternalSwap)}>
        <div class="text-2xl cursor-pointer"
            hx-get="/"
            hx-trigger="${TRIGGERS.changeRoute}"
            hx-swap="innerHTML"
            hx-target="#${ROOT_ID}"
            onclick="${pushRouteToHistoryIfNotFunction('/', 'index.html')}">
            ${Translations.defaultLocale.appTitle}
        </div>
        <div id="app-navigation-dropdown" class="cursor-pointer text-xl text-right relative w-fit">
                <div>${currentUser}</div>
                <ul class="${HIDDEN_CLASS} whitespace-nowrap absolute top-8 right-0 rounded-md shadow-md 
                    ${PROPS.bgColorSecondary2} ${PROPS.borderColorSecondary2}">
                    <li class="${dropDownElementClasses}"
                        hx-get="${PROFILE_ENDPOINT}"
                        hx-trigger="${TRIGGERS.changeRoute}"
                        hx-swap="innerHTML"
                        hx-target="#${ROOT_ID}"
                        onclick="${pushRouteToHistoryIfNotFunction(PROFILE_ENDPOINT)}">
                        ${Translations.defaultLocale.navigation.profile}
                    </li>
                    <li class="${dropDownElementClasses}"
                        hx-post="${SIGN_OUT_ENDPOINT}"
                        hx-trigger="click"
                        hx-replace-url="${SIGN_IN_ENDPOINT}"
                        hx-swap="innerHTML"
                        hx-target="#${ROOT_ID}">
                        ${Translations.defaultLocale.navigation.signOut}
                    </li>
                </ul>
            </div>
    </div>`;
}

export function oobSwapIf(externalSwap: boolean): string {
    return `${externalSwap ? 'hx-swap-oob="true"' : ""}`;
}

export function pushRouteToHistoryIfNotFunction(...routes: string[]): string {
    const routesArgs = routes.map(e => `'${e}'`).join(", ");
    return `pushRouteToHistoryIfNot(this, ${routesArgs})`;
}

export function errorPage(errors: ErrorCode[], renderFullPage: boolean, currentUser: string | null): string {
    const page = `<div>
        <h1 class="text-2xl mt-2 mb-6 font-medium">${Translations.defaultLocale.errorsModal.header}</h1>
        ${errorsComponent(errors)}
    </div>`;
    return renderFullPage ? wrappedInMainPage(page, currentUser) : page;
}

export function formValidatedTrigger(formLabel: string, valid: boolean) {
    return JSON.stringify({
        "form-validated": {
            "label": formLabel,
            "valid": valid
        }
    });
}

export function resetFormTrigger(label: string, ...additionalTriggers: any): string {
    return JSON.stringify({
        "reset-form": label,
        ...additionalTriggers
    });
}

export function additionalTrigersOfKeys(...keys: string[]): any {
    const jsonBody = [...keys].map(k => `"${k}": true`).join(",\n");
    return JSON.parse(`{ ${jsonBody} }`);
}

export function testIdAttribute(value: string): string {
    return `data-testid="${value}"`;
}