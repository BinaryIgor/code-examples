console.log("Some global js...");

const errorModal = document.getElementById("error-modal");

console.log("Have an error modal:", errorModal);

document.addEventListener("htmx:afterRequest", e => {
    console.log("After htmx request...", e);

    if (e.detail.failed) {
        console.log("HTMX request has failed!", e.detail);
        const error = e.detail.xhr.response;
        errorModal.show({message: error});
    }
});