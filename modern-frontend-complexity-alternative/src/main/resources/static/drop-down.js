class DropDown extends HTMLElement {

    #hideOnOutsideClick = undefined;

    connectedCallback() {
        const anchor = this.querySelector("[data-drop-down-anchor]");
        const options = this.querySelector("[data-drop-down-options]");

        anchor.onclick = () => options.classList.toggle("hidden");

        this.#hideOnOutsideClick = (e) => {
            if (e.target != anchor) {
                options.classList.add("hidden");
            }
        };

        window.addEventListener("click", this.#hideOnOutsideClick);
    }

    disconnectedCallback() {
        window.removeEventListener("click", this.#hideOnOutsideClick);
    }
}

if (customElements.get("drop-down") === undefined) {
    customElements.define("drop-down", DropDown);
}