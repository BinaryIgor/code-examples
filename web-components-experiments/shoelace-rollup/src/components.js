export class SomeComponent extends HTMLElement {

    connectedCallback() {
        console.log("Some empty component...");
        this.innerHTML = `<div>Some Empty Component</div>`;
    }
}
customElements.define('some-component', SomeComponent);