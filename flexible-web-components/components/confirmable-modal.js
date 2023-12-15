import { Components } from "./base.js";

const containerClass = "fixed z-10 left-0 top-0 w-full h-full overflow-auto";
const containerClassDefault = "bg-black/50";
const contentClassDefault = "m-auto mt-16 p-2 w-4/5 md:w-3/5 lg:w-2/5 bg-white rounded";
const titleClassDefault = "text-2xl font-bold mb-2 px-4 pt-4";
const messageClassDefault = "text-lg px-4";
const closeClass = "absolute top-0 right-0";
const closeClassDefault = "cursor-pointer text-3xl px-4";
const cancelOkContainerClassDefault = "mt-4";
const cancelClassDefault = "cursor-pointer text-lg";
const okClassDefault = "cursor-pointer text-lg";

const SHOW_EVENT = "show-confirmable-modal";
const HIDDEN_EVENT = "confirmable-modal-hidden";

class ConfirmableModal extends HTMLElement {

    constructor() {
        super();

        this._hidden = !this.hasAttribute("visible");

        this._render();

        this.onCancel = () => this.hide();
        this.onOk = () => { };
    }

    _render(title, message, cancel, ok) {
        const titleToRender = title ? title : Components.attributeValueOrDefault(this, "title", "Default title");
        const messageToRender = message ? message : Components.attributeValueOrDefault(this, "message", "Default message");
        const cancelToRender = cancel ? cancel : Components.attributeValueOrDefault(this, "cancel", "Cancel");
        const okToRender = ok ? ok : Components.attributeValueOrDefault(this, "ok", "Ok");

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

        const cancelOkContainerAttributes = Components.mappedAttributes(this, "cancel-ok-container", {
            defaultClass: cancelOkContainerClassDefault
        });
        const cancelAttributes = Components.mappedAttributes(this, "cancel", {
            toAddClass: cancelClassDefault
        });
        const okAttributes = Components.mappedAttributes(this, "ok", {
            toAddClass: okClassDefault
        });

        this.innerHTML = `
        <div ${this._hidden ? `style="display: none;"` : ""} ${containerAttributes}>
            <div style="position: relative;" ${contentAttributes}>
                <span ${closeAttributes}>${closeIcon}</span>
                <div ${titleAttributes}>${titleToRender}</div>
                <div ${messageAttributes}>${messageToRender}</div>
                <div style="display: flex; justify-content: space-between" ${cancelOkContainerAttributes}>
                    <div ${cancelAttributes} ${Components.renderedCustomIdAttribute("cancel")}>${cancelToRender}</div>
                    <div ${okAttributes} ${Components.renderedCustomIdAttribute("ok")}>${okToRender}</div>
              </div>
            </div>
        </div>
        `;

        this._container = this.querySelector("div");
        this._close = this.querySelector("span");
        this._cancel = Components.queryByCustomId(this, "cancel");
        this._ok = Components.queryByCustomId(this, "ok");

        this._showOnEvent = (e) => {
            const eDetail = e.detail;
            if (!this.id || this.id == eDetail.targetId) {
                this._render(eDetail.title, eDetail.message, eDetail.cancel, eDetail.ok);
                this._container.style.display = "block";
            }
        };

        this.show = ({ title = "", message = "", cancel = "", ok = "" }) => {
            this._render(title, message, cancel, ok);
            this._container.style.display = "block";
        };

        this.hide = e => {
            if (e == undefined || e.target == this._container) {
                this._container.style.display = "none";
                window.dispatchEvent(new CustomEvent(HIDDEN_EVENT, { detail: { id: this.id } }));
            }
        };

        this._close.onclick = () => this._container.style.display = "none";

        this._cancel.onclick = () => this.onCancel();
        this._ok.onclick = () => this.onOk();
    }

    connectedCallback() {
        window.addEventListener("click", this.hide);
        window.addEventListener(SHOW_EVENT, this._showOnEvent);
    }

    disconnectedCallback() {
        window.removeEventListener("click", this.hide);
        window.removeEventListener(SHOW_EVENT, this._showOnEvent);
    }
}

customElements.define("confirmable-modal", ConfirmableModal);