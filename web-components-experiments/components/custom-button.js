customElements.define("custom-button", class extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `<button class="m-4 p-4 text-2x border-4 rounded-md">It's a custom button</button>`;
    }
});