class SelectedContainer extends HTMLElement {

    #selectedValues = [];
    #selectedValuesToKeys = new Map();

    get selectedValues() {
        return this.#selectedValues;
    }

    connectedCallback() {
        this.#render();

        this.addEventListener("selected-container-change-selection", e => {
            const { key, value, selected } = e.detail;
            const beforeLength = this.#selectedValues.length;
            if (selected && !this.#selectedValues.includes(value)) {
                this.#selectedValues = [...this.#selectedValues, value];
                this.#selectedValuesToKeys.set(value, key);
            } else {
                this.#selectedValues = this.#selectedValues.filter(v => v != value);
                this.#selectedValuesToKeys.delete(value);
            }

            if (this.#selectedValues.length != beforeLength) {
                this.#render();
            }
        });
    }

    #render() {
        if (!this.#selectedValues) {
            this.innerHTML = null;
            return;
        }

        const selectedTemplateId = this.getAttribute("data-selected-template-id") ?? "selected-template-id";
        const selectedTemplate = document.getElementById(selectedTemplateId);

        this.innerHTML = this.#selectedValues.map(v => selectedTemplate.innerHTML.replaceAll("__value__", v)).join("\n");

        [...this.querySelectorAll("[data-selected]")].forEach(e => {
            const selectedValueElement = e.querySelector("[data-selected-value]");
            let selectedValue;
            if (selectedValueElement) {
                selectedValue = selectedValueElement.getAttribute("data-selected-value");
            } else {
                selectedValue = null;
            }
            const unselectButton = e.querySelector("[data-unselect-button]");
            if (unselectButton) {
                unselectButton.onclick = () => {
                    this.#selectedValues = this.#selectedValues.filter(v => v != selectedValue);
                    const key = this.#selectedValuesToKeys.get(selectedValue);
                    this.#render();
                    this.dispatchEvent(new CustomEvent("selected-container-selection-changed",
                        { detail: { key, value: selectedValue, selected: false } }
                    ));
                };
            }
        });
    }
}

if (customElements.get("selected-container") == undefined) {
    customElements.define("selected-container", SelectedContainer);
}