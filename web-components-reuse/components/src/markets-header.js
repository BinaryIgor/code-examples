class MarketsHeader extends HTMLElement {

    _denomination = 'USD';
    _liveUpdatesEnabled = true;
    _denominationExchangeRates = [];
    _liveUpdatesEnabledElement = undefined;
    _denominationElement = undefined;


    set denomination(value) {
        this._denomination = value;
        if (this._denominationElement) {
            this._denominationElement = this._denomination;
        }
    }

    set denominationExchangeRates(value) {
        this._denominationExchangeRates = value;
        this._renderDenominationOptions();
    }

    connectedCallback() {
        this.innerHTML = `
        <div class="absolute right-2 text-xl">Live Updates: <span class="cursor-pointer">${this._liveUpdatesElementText()}</span>
        </div>
        <span class="text-3xl">Markets in </span>
        <drop-down-container>
            <span class="underline cursor-pointer text-3xl pr-24">${this._denomination}</span>
            <ul class="border-1 rounded cursor-pointer bg-white text-lg" data-drop-down-options>
                ${this._denominationOptionsHTML()}
            </ul>
        </drop-down-container>
        `;

        this._liveUpdatesEnabledElement = this.querySelector("span");
        this._liveUpdatesEnabledElement.onclick = () => {
            this._liveUpdatesEnabled = !this._liveUpdatesEnabled;
            this._liveUpdatesEnabledElement.textContent = this._liveUpdatesElementText();
            document.dispatchEvent(new CustomEvent('mh:live-updates-toggled', { detail: this._liveUpdatesEnabled }));
        };

        this._denominationElement = this.querySelector("drop-down-container > span");
    }

    _denominationOptionsHTML() {
        return this._denominationExchangeRates.map(der => `<li class="py-2 px-4 border-b-1 last:border-0" data-option-id="${der.name}">${der.name}: ${der.exchangeRate}</li>`).join('\n');
    }

    _renderDenominationOptions() {
        const optionsContainer = this.querySelector("ul");
        if (optionsContainer) {
            optionsContainer.innerHTML = this._denominationOptionsHTML();
            this._setOptionsClickHandlers();
        }
    }

    _setOptionsClickHandlers() {
        [...this.querySelectorAll("li")].forEach(o => {
            o.onclick = () => {
                this._denomination = o.getAttribute("data-option-id");
                this._denominationElement.textContent = this._denomination;
                document.dispatchEvent(new CustomEvent('mh:denomination-changed', { detail: this._denomination }));
            };
        });
    }

    _liveUpdatesElementText() {
        return `${this._liveUpdatesEnabled ? "ON" : "OFF"}`;
    }
}

export function register() {
    customElements.define("markets-header", MarketsHeader);
}