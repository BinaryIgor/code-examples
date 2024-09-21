customElements.define("web-component-page", class extends HTMLElement {

    _counter = 0;

    connectedCallback() {
        const initialHTMXCounter = this.getAttribute("data-htmx-counter") || 0;
        this.innerHTML = `
        <h1>Web Components Page</h1>
        <div class="my-4">${this._counterStateText()}</div>
        <button class="ml-2 my-4 px-8 py-2 text-4xl border-2">-</button>
        <button class="mr-2 my-4 px-8 py-2 text-4xl border-2">+</button>
        
        <div class="my-4" id="htmx-counter">HTMX Counter: ${initialHTMXCounter}</div>
        <button class="ml-2 my-4 px-8 py-2 text-4xl border-2" hx-post="/decrease-counter" hx-target="#htmx-counter">-</button>
        <button class="mr-2 my-4 px-8 py-2 text-4xl border-2" hx-post="/increase-counter" hx-target="#htmx-counter">+</button>
        `;

        this._counterDiv = this.querySelector("div");

        const [decreaseCounterButton, increaseCounterButton] = this.querySelectorAll("button");
        decreaseCounterButton.onclick = () => {
            this._updateCounter(this._counter - 1);
        };
        increaseCounterButton.onclick = () => {
            this._updateCounter(this._counter + 1);
        };
    }

    _counterStateText() {
        return `Counter: ${this._counter}`;
    }

    _updateCounter(newValue) {
        this._counter = newValue;
        this._counterDiv.innerHTML = this._counterStateText();
    }
});