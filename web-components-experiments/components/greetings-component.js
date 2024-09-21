customElements.define("greetings-component", class extends HTMLElement {
    connectedCallback() {
        this.innerHTML = `
            <h2>Hello from the Custom Element!</h2>
            <div>${new Date()}</div>
        `;
    }
});