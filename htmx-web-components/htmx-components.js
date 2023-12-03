const defaultErrorClass = "htmx-components__error";
const defaultHiddenClass = "htmx-components__hiden";

const styles = `
    .${defaultErrorClass} {
        color: red;
    }
    .${defaultHiddenClass} {
        display: none;
    }
`;
document.head.appendChild(document.createElement("style")).innerHTML = styles;

const HTMX = {
    events: {
        beforeRequest: "htmx:beforeRequest",
        afterRequest: "htmx:afterRequest",
        responseError: "htmx:responseError"
    },
    requestPathFromEvent(event) {
        return event.detail.pathInfo.requestPath;
    },
    isFailedRequest(event) {
        return event.detail.failed;
    },
    isRequestFromPath(event, desiredPath) {
        return this.requestPathFromEvent(event) == desiredPath;
    },
    requestResponseFromEvent(event) {
        return event.detail.xhr.response;
    }
};

class HTMXElement extends HTMLElement {
    constructor() {
        super();
    }

    getAttributeOrDefault(attribute, defaultValue="") {
        const attrValue = this.getAttribute(attribute);
        return attrValue ? attrValue : defaultValue;
    }

    getElementClassOrDefault(suffix, defaultValue) {
        return this.getAttributeOrDefault(`class-${suffix}`, defaultValue);
    }

    getToRenderHTMXAttribute(sourceAttribute, htmxAttribute=null) {
        const value = this.getAttribute(sourceAttribute);
        if (!value) {
            return "";
        }
        
        let toUseAttribute;
        if (htmxAttribute) {
            toUseAttribute = htmxAttribute;
        } else {
            toUseAttribute = sourceAttribute.replace("_", "");
        }

        return `${toUseAttribute}="${value}"`;
    }

    renderedAttributeIfSourceNotEmpty(targetAttribute, sourceAttribute) {
        const sourceAttributeValue = this.getAttribute(sourceAttribute);
        return sourceAttributeValue ? `${targetAttribute}="${sourceAttributeValue}"` : "";
    } 

    renderedElementClassIfSourceNotEmpty(classSufix) {
        return this.renderedAttributeIfSourceNotEmpty("class", `class-${classSufix}`);
    } 
}

class SingleInputError extends HTMXElement {

    static observedAttributes = ["message"];

    constructor() {
        super();
        this._hiddenClass = this.getElementClassOrDefault("hidden", defaultHiddenClass);
        this._errorClass = this.getElementClassOrDefault("error", defaultErrorClass);
        this._render();
    }

    _render() {
        const message = this.getAttributeOrDefault("message");
        const hiddenClass = message ? "" : this._hiddenClass;

        this.innerHTML = `<p class="${this._errorClass} ${hiddenClass}">${message}</p>`;
    }

    attributeChangedCallback(name, oldValue, newValue) {
        this._render();
    }
}

class CustomForm extends HTMXElement {
    constructor() {
        super();

        this._postFormPath = this.getAttribute("_hx-post");
        const hxPost = this.getToRenderHTMXAttribute("_hx-post");

        const hxTarget = this.getToRenderHTMXAttribute("_hx-target");
        const hxSwap = this.getToRenderHTMXAttribute("_hx-swap");

        const idValidationHxPost = this.getToRenderHTMXAttribute("_hx-post-id-validation", "hx-post");

        const nameValidationHxPost = this.getToRenderHTMXAttribute("_hx-post-name-validation", "hx-post");

        const renderInput = (hxPost, name, placeholder) =>
            `<input style="display: block" ${this.renderedElementClassIfSourceNotEmpty(`${name}-input`)}
            hx-trigger="input changed delay:500ms" 
            hx-swap="outerHTML" 
            hx-target="next single-input-error" 
            ${hxPost}
            name="${name}" placeholder="${placeholder}">`;

        this.innerHTML = `
            <div ${this.renderedElementClassIfSourceNotEmpty("container")}">
                <single-input-error ${this.renderedAttributeIfSourceNotEmpty("class-error", "class-generic-error")}></single-input-error>
                <form ${hxPost} ${hxTarget} ${hxSwap}>
                    ${renderInput(idValidationHxPost, "id", "id")}
                    <single-input-error></single-input-error>
                    ${renderInput(nameValidationHxPost, "name", "name")}
                    <single-input-error></single-input-error>
                    <input ${this.renderedElementClassIfSourceNotEmpty("submit-input")} type="submit" value="Add new item">
                </form>
            <div>
        `;

        this._genericError = this.querySelector("single-input-error");
    }

    connectedCallback() {
        this.clearInputs = (e) => {
            const inputs = this.querySelectorAll(`input:not([type="submit"])`);
            if (HTMX.isRequestFromPath(e, this._postFormPath) && !HTMX.isFailedRequest(e)) {
                inputs.forEach(i => i.value = "");
            }
        };

        this.showGenericError = (e) => {
            if (HTMX.isRequestFromPath(e, this._postFormPath)) {
                this._setGenericErrorMessage(HTMX.requestResponseFromEvent(e));
            }
        };

        this.addEventListener(HTMX.events.afterRequest, this.clearInputs);
        this.addEventListener(HTMX.events.responseError, this.showGenericError);

        this.querySelectorAll("input").forEach(i => {
            i.addEventListener("input", () => {
                this._setGenericErrorMessage("");
            });
        });
    }

    _setGenericErrorMessage(message) {
        this._genericError.setAttribute("message", message);
    }

    disconnectedCallback() {
        this.removeEventListener(HTMX.events.afterRequest, this.clearInputs);
        this.removeEventListener(HTMX.events.responseError, this.showGenericError);
    }
}

class ItemElement extends HTMXElement {
    constructor() {
        super();
        const id = this.getAttribute("item-id");
        const name = this.getAttribute("item-name");
        this.innerHTML = `<li ${this.renderedElementClassIfSourceNotEmpty("item")}>Id: ${id}, Name: ${name}</li>`;
    }
}

class ItemsList extends HTMXElement {
    constructor() {
        super();
        this.innerHTML = `
        <ul ${this.renderedElementClassIfSourceNotEmpty("container")}>
            ${this.innerHTML}
        </ul>
        `;
    }
}

customElements.define("single-input-error", SingleInputError);
customElements.define("custom-form", CustomForm);
customElements.define("item-element", ItemElement);
customElements.define("items-list", ItemsList);