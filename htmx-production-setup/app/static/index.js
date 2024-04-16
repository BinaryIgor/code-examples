console.log("Some global js...");

const HIDDEN_CLASS = "hidden";

const HTMX_EVENTS = {
    confirm: "htmx:confirm",
    afterSwap: "htmx:afterSwap",
    historyRestored: "htmx:historyCacheMissLoad",
    load: "htmx:load",
    responseError: "htmx:responseError",
    sendError: "htmx:sendError",
    pushedIntoHistory: "htmx:pushedIntoHistory"
};

const TRIGGERS = {
    changeRoute: "change-route",
    resetScroll: "reset-scroll",
    formValidated: "form-validated",
    resetForm: "reset-form"
};

const errorModal = document.getElementById("error-modal");
const navigation = document.getElementById("app-navigation");

document.addEventListener("htmx:afterRequest", e => {
    console.log("After htmx request...", e);

    if (e.detail.failed) {
        console.log("HTMX request has failed!", e.detail);
        const error = e.detail.xhr.response;
        errorModal.show({message: error});
    }
});

document.addEventListener("top-navigation-show", e => {
    console.log("Should show top navigation...");
    navigation.classList.remove(HIDDEN_CLASS);
});

document.addEventListener("top-navigation-hide", e => {
    console.log("Should hide top navigation...");
     navigation.classList.add(HIDDEN_CLASS);
});

function pushHomeIfNotAtHome(el) {
    if (location.pathname != "/") {
        el.setAttribute("hx-push-url", "/");
    } else {
        el.removeAttribute("hx-push-url");
    }
    el.dispatchEvent(new Event("render-home"));
}