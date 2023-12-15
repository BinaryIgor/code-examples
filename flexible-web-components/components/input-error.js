import { Components } from "./base.js";

const errorClassDefault = "text-red-500";

export class InputError extends HTMLElement {

    static observedAttributes = ["message"];

    constructor() {
        super();
        const message = Components.attributeValueOrDefault(this, "message");
        this._render(message);
    }

    _render(message) {
        const errorAttributes = Components.mappedAttributes(this, "error", { defaultClass: errorClassDefault });
        this.innerHTML = `<p ${message ? `` : `style="display: none"`} ${errorAttributes}>${message}</p>`;
    }

    attributeChangedCallback(name, oldValue, newValue) {
        this._render(newValue);
    }
}

customElements.define("input-error", InputError);