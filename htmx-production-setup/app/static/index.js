const HIDDEN_CLASS = "hidden";

const errorModal = document.getElementById("error-modal");
const navigation = document.getElementById("app-navigation");

document.addEventListener("htmx:afterRequest", e => {
    if (e.detail.failed) {
        const error = e.detail.xhr.response;
        errorModal.show({message: error});
    }
});

document.addEventListener("top-navigation-show", e => navigation.classList.remove(HIDDEN_CLASS));

document.addEventListener("top-navigation-hide", e => navigation.classList.add(HIDDEN_CLASS));

function pushHomeIfNotAtHome(el) {
    if (location.pathname != "/") {
        el.setAttribute("hx-push-url", "/");
    } else {
        el.removeAttribute("hx-push-url");
    }
    el.dispatchEvent(new Event("render-home"));
}