class ErrorModal extends HTMLElement {

    #modal = null;
    #onShowHandler = null;

    connectedCallback() {
        this.innerHTML = `<info-modal title-class="text-red-500"></info-modal>`;

        this.#modal = this.querySelector("info-modal");
        this.#onShowHandler = (e) => {
            const { title, error } = e.detail;
            this.#modal.show({ title, content: error });
        };

        document.addEventListener('em.show', this.#onShowHandler);
    }

    disconnectedCallback() {
        document.removeEventListener('em.show', this.#onShowHandler);
    }
}

export function register() {
    customElements.define('error-modal', ErrorModal);
}