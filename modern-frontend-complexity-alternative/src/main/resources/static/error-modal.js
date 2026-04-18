class ErorrModal extends HTMLElement {

    #container = undefined;
    #titleElement = undefined;
    #contentElement = undefined;

    connectedCallback() {
        this.#container = this.querySelector("[data-container]");
        this.#titleElement = this.querySelector("[data-title]");
        this.#contentElement = this.querySelector("[data-content]");

        this.querySelector("[data-close-button]").onclick = () => {
            this.#container.classList.add("hidden");
        };

        this.addEventListener("error-modal-show", e => {
            const { title, content } = e.detail;
            if (title) {
                this.#titleElement.innerHTML = title;
            }
            if (content) {
                this.#contentElement.innerHTML = content;
            }
            this.#container.classList.remove("hidden");
        });
    }
}

if (customElements.get("error-modal") === undefined) {
    customElements.define("error-modal", ErorrModal);
}