class TabHeader extends HTMLElement {

    connectedCallback() {
        this.classList.add("text-2xl");
        this.classList.add("p-2");
        this.classList.add("cursor-pointer");
        this.classList.add("grow");
    }
}

export function register() {
    customElements.define('tab-header', TabHeader);
}