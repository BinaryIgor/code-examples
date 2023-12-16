import { Components } from "./base.js";

const inputClassDefault = "rounded p-2 border-2 border-solid border-slate-100 focus:outline-slate-300";

//Dependencies: registered input-error
export class InputWithError extends HTMLElement {

    constructor() {
        super();

        const containerAttributes = Components.mappedAttributes(this, "container");
        const inputAttributes = Components.mappedAttributes(this, "input", {
            defaultClass: inputClassDefault
        });
        const errorAttributes = Components.mappedAttributes(this, "error", { keepSufix: true });

        this.innerHTML = `
        <div ${containerAttributes}>
            <input ${inputAttributes}></input>
            <input-error ${errorAttributes}></input-error>
        </div>
        `;

        this._input = this.querySelector("input");
        this._inputError = this.querySelector("input-error");

        this.onInputValidated = (error) => {
            this._inputError.setAttribute("message", error);
        };
    }

    connectedCallback() {
        this._input.addEventListener("input", e => {
            if (this.onInputChanged) {
                this.onInputChanged(this._input.value);
            }
        });
    }

    attributeChangeCallback(name, oldValue, newValue) {
        this.onInputChanged(newValue);
    }

    set value(value) {
        this._input.value = value;
    }
}

customElements.define("input-with-error", InputWithError);
