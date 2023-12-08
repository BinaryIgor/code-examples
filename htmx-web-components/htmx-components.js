const defaultErrorClass = prefixedClass("error");
const hiddenClass = prefixedClass("hiden");
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
    .${hiddenClass} {
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
    isRequestFromElement(event, element) {
        return event.target == element;
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

    getElementClassOrDefault(element, defaultValue = "") {
        return this.getAttributeOrDefault(`class-${element}`, defaultValue);
    }

    renderedAttributeIfSet(targetAttribute, sourceAttribute) {
        const sourceAttributeValue = this.getAttribute(sourceAttribute);
        return sourceAttributeValue ? `${targetAttribute}="${sourceAttributeValue}"` : "";
    }

    renderedElementClassIfSet(element) {
        return this.renderedAttributeIfSet("class", `class-${element}`);
    }

    renderedElementHTMXAttributes(element) {
        const htmxAttributes = this.getAttributeNames()
            .filter(a => a.startsWith("hx-") && a.endsWith(element));

        if (htmxAttributes.length == 0) {
            return "";
        }

        return htmxAttributes.map(a => {
            const key = a.replace(`-${element}`, "");
            const value = this.getAttribute(a);
            return `${key}="${value}"`;
        }).join("\n");
    }
}

// class CustomModal extends HTMXElement {

//     constructor() {
//         super();
//         this._modalContainerClass = `${modalContainerClass} ${this.getElementClassOrDefault("container")}`;
//         this._modalContentClass = this.getElementClassOrDefault("content", defaultModalContentClass);
//         this._modalCloseClass = `${modalCloseClass} ${this.getElementClassOrDefault("close")}`;
//         this._render();
//     }

//     _render(title, message, titleAdditionalClass, messageAdditionalClass) {
//         const titleToRender = title ? title : "Default title";
//         const messageToRender = message ? message : "Default message";

//         const titleClass = `${this.getElementClassOrDefault("title")} ${titleAdditionalClass ? titleAdditionalClass : ""}`;
//         const messageClass = `${this.getElementClassOrDefault("message")} ${messageAdditionalClass ? messageAdditionalClass : ""}`;

//         this.innerHTML = `
//         <div class="${this._modalContainerClass}">
//             <div style="position: relative;" class="${this._modalContentClass}">
//               <span class="${this._modalCloseClass}">&times;</span>
//               <div class="${titleClass}">${titleToRender}</div>
//               <div class="${messageClass}">${messageToRender}</div>
//             </div>
//         </div>
//         `;
//     }

//     connectedCallback() {
//         this.showModal = (e) => {
//             const eDetail = e.detail;
//             this._render(eDetail.title, eDetail.message, eDetail.titleAdditionalClass, eDetail.messageAdditionalClass);
//             this._modalContainer().style.display = "block";
//             this.querySelector("span").onclick = () => this._modalContainer().style.display = "none";
//         }

//         this.hideModal = e => {
//             const modal = this._modalContainer();
//             if (e.target == modal) {
//                 modal.style.display = "none";
//             }
//         }

//         window.addEventListener("click", this.hideModal);
//         window.addEventListener("show-custom-modal", this.showModal);
//     }

//     _modalContainer() {
//         return this.querySelector("div");
//     }

//     disconnectedCallback() {
//         window.removeEventListener("click", this.hideModal);
//         window.removeEventListener("show-custom-modal", this.showModal);
//     }
// }

class InputError extends HTMXElement {

    static observedAttributes = ["message"];

    constructor() {
        super();
        const message = this.getAttributeOrDefault("message");
        this._render(message);
    }

    _render(message) {
        const errorClass = this.getElementClassOrDefault("error", defaultErrorClass);
        this.innerHTML = `<p class="${errorClass} ${message ? "" : hiddenClass}">${message}</p>`;
    }

    attributeChangedCallback(name, oldValue, newValue) {
        this._render(newValue);
    }
}

class ItemForm extends HTMXElement {
    constructor() {
        super();
        this.innerHTML = `
        <div ${this.renderedElementClassIfSet("container")}>
            <input-error ${this.renderedAttributeIfSet("class-error", "class-generic-error")}></input-error>
            <form ${this.renderedElementHTMXAttributes("form")}>
                ${this._renderedInput("id", "Item id")}
                <input-error></input-error>
                ${this._renderedInput("name", "Item name")}
                <input-error></input-error>
                <input ${this.renderedElementClassIfSet("submit")} type="submit" value="Add item">
            </form>
        <div>
        `;
    }

    _renderedInput(name, placeholder) {
        const inputId = `${name}-input`;
        return `<input ${this.renderedElementHTMXAttributes(inputId)}
            ${this.renderedElementClassIfSet(inputId)}
            hx-trigger="input changed delay:500ms"
            hx-target="next input-error"
            hx-swap="outerHTML"
            style="display: block" name="${name}" placeholder="${placeholder}">`;
    }

    connectedCallback() {
        const from = this.querySelector("form");
        const genericError = this.querySelector("input-error");
        const inputs = this.querySelectorAll(`input:not([type="submit"])`);

        this._afterRequestListener = e => {
            if (!HTMX.isRequestFromElement(e, from)) {
                return;
            }
            if (HTMX.isFailedRequest(e)) {
                const error = HTMX.requestResponseFromEvent(e);
                genericError.setAttribute("message", error);
            } else {
                inputs.forEach(i => i.value = "");
            }
        };

        this.addEventListener(HTMX.events.afterRequest, this._afterRequestListener);

        inputs.forEach(i => {
            i.addEventListener("input", i => {
                genericError.setAttribute("message", "");
            });
        });
    }


    disconnectedCallback() {
        this.removeEventListener(HTMX.events.afterRequest, this._afterRequestListener);
    }
}

class ItemElement extends HTMXElement {
    constructor() {
        super();
        const id = this.getAttribute("item-id");
        const name = this.getAttribute("item-name");

        this.innerHTML = `<li ${this.renderedElementClassIfSet("item")}>Id: ${id}, Name: ${name}</li>`;

        this.querySelector("li").onclick = () => {
            window.dispatchEvent(new CustomEvent("item-element-clicked", { detail: { id, name } }));
        };
    }
}

class ItemsList extends HTMXElement {
    constructor() {
        super();
        this.innerHTML = `
        <ul ${this.renderedElementClassIfSet("container")}>
            ${this.innerHTML}
        </ul>
        `;
    }
}

customElements.define("input-error", InputError);
customElements.define("item-form", ItemForm);
customElements.define("item-element", ItemElement);
customElements.define("items-list", ItemsList);