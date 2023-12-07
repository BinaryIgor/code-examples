const defaultErrorClass = prefixedClass("error");
const defaultHiddenClass = prefixedClass("hiden");
const modalContainerClass = prefixedClass("modal");
const defaultModalContentClass = prefixedClass("modal-content");
const modalCloseClass = prefixedClass("close");
const defaultModalCloseClass = prefixedClass("default-close");

function prefixedClass(className) {
    return `htmx-components__${className}`
}

const styles = `
    .${defaultErrorClass} {
        color: red;
    }
    .${defaultHiddenClass} {
        display: none;
    }
    .${modalContainerClass} {
        display: none;
        position: fixed;
        z-index: 1;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        overflow: auto;
    }
    .${defaultModalContentClass} {
        margin: auto;
        margin-top: 10%;
        padding: 8px;
        width: 80%;
        background-color: white;
    }
    .${modalCloseClass} {
        position: absolute;
        top: 0;
        right: 0;
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

    getAttributeOrDefault(attribute, defaultValue = "") {
        const attrValue = this.getAttribute(attribute);
        return attrValue ? attrValue : defaultValue;
    }

    getElementClassOrDefault(suffix, defaultValue) {
        return this.getAttributeOrDefault(`class-${suffix}`, defaultValue);
    }

    getToRenderHTMXAttribute(sourceAttribute, htmxAttribute = null) {
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

class CustomModal extends HTMXElement {

    constructor() {
        super();
        this._modalContainerClass = `${modalContainerClass} ${this.getElementClassOrDefault("container")}`;
        this._modalContentClass = this.getElementClassOrDefault("content", defaultModalContentClass);
        this._modalCloseClass = `${modalCloseClass} ${this.getElementClassOrDefault("close")}`;
        this._render();
    }

    _render(title, message) {
        const titleToRender = title ? title : "Default title";
        const messageToRender = message ? message : "Default message";

        this.innerHTML = `
        <div class="${this._modalContainerClass}">
            <div style="position: relative;" class="${this._modalContentClass}">
              <span class="${this._modalCloseClass}">&times;</span>
              <div ${this.renderedElementClassIfSourceNotEmpty("title")}>${titleToRender}</div>
              <div ${this.renderedElementClassIfSourceNotEmpty("message")}>${messageToRender}</div>
            </div>
        </div>
        `;
    }

    connectedCallback() {
        this.showModal = (e) => {
            console.log("Show modal..")
            this._render(e.detail.title, e.detail.message);
            this._modalContainer().style.display = "block";
            this.querySelector("span").onclick = () => this._modalContainer().style.display = "none";
        }

        this.hideModal = e => {
            const modal = this._modalContainer();
            if (e.target == modal) {
                modal.style.display = "none";
            }
        }

        window.addEventListener("click", this.hideModal);
        window.addEventListener("show-custom-modal", this.showModal);
    }

    _modalContainer() {
        return this.querySelector("div");
    }

    disconnectedCallback() {
        window.removeEventListener("click", this.hideModal);
        window.removeEventListener("show-custom-modal", this.showModal);
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

customElements.define("custom-modal", CustomModal);
customElements.define("single-input-error", SingleInputError);
customElements.define("custom-form", CustomForm);
customElements.define("item-element", ItemElement);
customElements.define("items-list", ItemsList);