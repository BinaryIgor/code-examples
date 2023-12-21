class CustomMessage extends HTMLElement {

    static observedAttributes = ["category", "message"];

    constructor() {
        super();
        const category = this.getAttribute("category");
        const message = this.getAttribute("message");
        this.innerHTML = `
        <div>You've got an interesting message, from ${category} category:</div>
        <div>${message}</div>
        `;
    }

    attributeChangedCallback(name, oldValue, newValue) {
        console.log(`${name} attribute was changed from ${oldValue} to ${newValue}!`);
    }

    connectedCallback() {
        console.log("Element was added to the DOM!");
    }

    disconnectedCallback() {
        console.log("Element was removed from the DOM!");
    }
}

customElements.define('custom-message', CustomMessage);