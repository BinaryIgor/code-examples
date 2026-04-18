class SelectablesContainer extends HTMLElement {

    connectedCallback() {
        let selectedClass = this.getAttribute("data-selected-class");
        if (!selectedClass) {
            selectedClass = "underline";
        }

        const selectables = this.querySelectorAll("[data-selectable]");
        for (let s of selectables) {
            const key = s.getAttribute("data-selectable-key");
            const value = s.getAttribute("data-selectable-value");

            s.addEventListener("click", e => {
                e.stopPropagation();
                const selected = s.classList.toggle(selectedClass);
                this.dispatchEvent(new CustomEvent("selectables-container-selection-changed", {
                    detail: { key, value, selected }
                }));
            });
        }

        this.addEventListener("selectables-container-change-selection", e => {
            const { selected, value } = e.detail;
            const selectableElement = this.querySelector(`[data-selectable-value="${value}"]`);
            if (!selectableElement) {
                return;
            }
            if (selected) {
                selectableElement.classList.add(selectedClass);
            } else {
                selectableElement.classList.remove(selectedClass);
            }
        });
    }
}

if (customElements.get("selectables-container") === undefined) {
    customElements.define("selectables-container", SelectablesContainer);
}