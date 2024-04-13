console.log("Some global js...");

const HIDDEN_CLASS = "hidden";

const errorModal = document.getElementById("error-modal");
const navigation = document.getElementById("app-navigation");

console.log("Have an error modal:", errorModal);

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