// TODO: error styles
class InfoModal extends HTMLElement {

    #container = null;
    #closeButton = null;
    #titleElement = null;
    #contentElement = null;

    connectedCallback() {
        const titleClass = this.getAttribute('title-class');
        const title = this.getAttribute("title");
        const content = this.getAttribute("content");

        this.innerHTML = `
        <div data-container class="bg-black/70" style="position: fixed; top: 0; left: 0; height: 100%; width: 100%; z-index: 100; display: none">
            <div class="max-w-lg w-11/12 rounded top-1/2 left-1/2 absolute -translate-x-1/2 -translate-y-1/2 bg-white">
                <span data-close-button class="cursor-pointer text-4xl px-2" style="position: absolute; top: 0px; right: 0px">×</span>    
                <div data-title-element class="text-xl font-bold p-4${titleClass ? " " + titleClass : ""}">${title ? title : ''}</div>
                <div data-content-element class="px-4 pb-12">${content ? content : ''}</div>
            </div>
        </div>
        `;

        this.#container = this.querySelector('[data-container]');
        this.#closeButton = this.querySelector('[data-close-button]');
        this.#titleElement = this.querySelector('[data-title-element]');
        this.#contentElement = this.querySelector('[data-content-element]');

        this.#closeButton.onclick = () => this.close();
    }

    show({ title, content }) {
        if (title) {
            this.#titleElement.textContent = title;
        }
        if (content) {
            this.#contentElement.textContent = content;
        }
        this.#container.style.display = "block";
    }

    close() {
        this.#container.style.display = "none";
        this.dispatchEvent(new CustomEvent('im.closed', { bubbles: true }));
    }
}

export function register() {
    customElements.define('info-modal', InfoModal);
}