import { Components } from "./base.js";

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
        console.log("Clear inputs...", inputs);
        inputs.forEach(i => {
            i.value = "";
        });
    }

    afterSubmit({ error = "", alwaysClearInputs = false, showGenericError = false}) {
        console.log("After submit, error:", error);
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