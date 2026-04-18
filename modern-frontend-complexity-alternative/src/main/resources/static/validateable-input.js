class ValidateableInput extends HTMLElement {

    #validator = (input) => true;

    set validator(v) {
        this.#validator = v;
    }

    connectedCallback() {
        const inputElement = this.querySelector("[data-input]")
        const inputErrorElement = this.querySelector("[data-input-error]");

        inputElement.addEventListener("input", () => {
            if (this.#validator(inputElement.value)) {
                inputErrorElement.classList.add("hidden");
            } else {
                inputErrorElement.classList.remove("hidden");
            }
        });
    }

}

if (customElements.get("validateable-input") == undefined) {
    customElements.define("validateable-input", ValidateableInput);
}