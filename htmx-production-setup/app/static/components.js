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


const inputClassDefault = "rounded p-2 border-2 border-solid border-slate-100 focus:border-slate-300 outline-none";

//Dependencies: registered input-error
export class InputWithError extends HTMLElement {

    static observedAttributes = ["input:value"];

    constructor() {
        super();

        const containerAttributes = Components.mappedAttributes(this, "container");
        const inputAttributes = Components.mappedAttributes(this, "input", {
            defaultClass: inputClassDefault
        });
        const inputErrorAttributes = Components.mappedAttributes(this, "input-error");
        const errorAttributes = Components.mappedAttributes(this, "error");

        this.innerHTML = `
        <div ${containerAttributes}>
            <input ${inputAttributes}></input>
            <input-error ${inputErrorAttributes} ${errorAttributes}></input-error>
        </div>
        `;

        this._input = this.querySelector("input");
        this._inputError = this.querySelector("input-error");

        this.onInputValidated = (error) => {
            this._inputError.setAttribute("message", error);
        };

        this.onInputChanged = (input) => {
            if (this.inputValidator) {
                const error = this.inputValidator(input);
                this.onInputValidated(error);
            }
        };
    }

    connectedCallback() {
        this._input.addEventListener("input", e => {
            this.onInputChanged(this._input.value);
        });
    }

    attributeChangedCallback(name, oldValue, newValue) {
        this._input.value = newValue;
        this.onInputChanged(newValue);
    }
}

customElements.define("input-with-error", InputWithError);

const genericErrorClassDefault = "text-red-600 text-lg italic mt-4 my-2";
const hiddenClass = "hidden";

class FormContainer extends HTMLElement {

    constructor() {
        super();

        const errorAttributes = Components.mappedAttributes(this, "generic-error", {
            defaultClass: genericErrorClassDefault,
            toAddClass: hiddenClass
        });
        const formAttributes = Components.mappedAttributes(this, "form");
        const submitAttributes = Components.mappedAttributes(this, "submit", {
            defaultAttributes: {
                value: "Submit"
            }
        });

        this.innerHTML = `
        <form ${formAttributes}>
        ${this.innerHTML}
        <p ${Components.renderedCustomIdAttribute("generic-error")} ${errorAttributes}></p>
        <input type="submit" ${submitAttributes}>
        </form>
        `;

        this._genericError = Components.queryByCustomId(this, "generic-error");
        this._form = this.querySelector("form");
        this._submit = this.querySelector(`input[type="submit"]`);

        this._form.addEventListener("submit", e => {
            this._submit.disabled = true;
        });
    }

    clearInputs() {
        const inputs = [...this.querySelectorAll(`input:not([type="submit"])`)];
        inputs.forEach(i => {
            i.value = "";
        });
    }

    afterSubmit({ error = "", alwaysClearInputs = false, showGenericError = false}) {
        this._submit.disabled = false;

        if (alwaysClearInputs || !error) {
            this.clearInputs();
        }

        if (error && showGenericError) {
            this._genericError.textContent = error;
            this._genericError.classList.remove(hiddenClass);
        } else {
            this._genericError.classList.add(hiddenClass);
        }
    }
}

customElements.define("form-container", FormContainer);

const containerClass = "fixed z-10 left-0 top-0 w-full h-full overflow-auto";
const containerClassDefault = "bg-black/50";
const contentClassDefault = "m-auto mt-16 p-4 w-4/5 md:w-3/5 lg:w-2/5 bg-white rounded";
const titleClassDefault = "text-2xl font-bold mb-2";
const messageClassDefault = "text-lg";
const closeClass = "absolute top-0 right-0";
const closeClassDefault = "cursor-pointer text-3xl px-2";

const SHOW_EVENT = "show-info-modal";
const HIDDEN_EVENT = "info-modal-hidden";

class InfoModal extends HTMLElement {

    constructor() {
        super();
        this._render();
    }

    _render(title, message) {
        const titleToRender = title ? title : Components.attributeValueOrDefault(this, "title", "Default title");
        const messageToRender = message ? message : Components.attributeValueOrDefault(this, "message", "Default message");

        const containerAttributes = Components.mappedAttributes(this, "container", {
            toAddClass: containerClass,
            defaultClass: containerClassDefault
        });
        const contentAttributes = Components.mappedAttributes(this, "content", {
            defaultClass: contentClassDefault
        });

        const titleAttributes = Components.mappedAttributes(this, "title", { defaultClass: titleClassDefault });
        const messageAttributes = Components.mappedAttributes(this, "message", { defaultClass: messageClassDefault });

        const closeIcon = Components.attributeValueOrDefault(this, "close-icon", "&times;");
        const closeAttributes = Components.mappedAttributes(this, "close", {
            toAddClass: closeClass,
            defaultClass: closeClassDefault,
            toSkipAttributes: ["icon"]
        });

        this.innerHTML = `
        <div style="display: none;" ${containerAttributes}>
            <div style="position: relative;" ${contentAttributes}>
                <span ${closeAttributes}>${closeIcon}</span>
                <div ${titleAttributes}>${titleToRender}</div>
                <div ${messageAttributes}>${messageToRender}</div>
            </div>
        </div>`;

        this._container = this.querySelector("div");
        this._close = this.querySelector("span");
    }

    connectedCallback() {
        this._showOnEvent = (e) => {
            const eDetail = e.detail;
            if (!this.id || this.id == eDetail.targetId) {
                this.show(eDetail.title, eDetail.message);
            }
        }

        // defined here because of the this for window listener issues
        this.show = ({ title = "", message = "" } = {}) => {
            this._render(title, message);
            this._container.style.display = "block";
            this._close.onclick = () => this._container.style.display = "none";
        };

        this.hide = e => {
            if (e.target == this._container) {
                this._container.style.display = "none";
                window.dispatchEvent(new CustomEvent(HIDDEN_EVENT, { detail: { id: this.id } }));
            }
        };

        window.addEventListener("click", this.hide);
        window.addEventListener(SHOW_EVENT, this._showOnEvent);
    }

    disconnectedCallback() {
        window.removeEventListener("click", this.hide);
        window.removeEventListener(SHOW_EVENT, this._showOnEvent);
    }
}

customElements.define("info-modal", InfoModal);