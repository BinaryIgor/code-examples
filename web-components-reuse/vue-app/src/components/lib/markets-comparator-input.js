class MarketsComparatorInput extends HTMLElement {

    _assets = [];
    _currencies = [];
    _assetOrCurrencyOptions = [];
    _onChosenMarketSizeChange = (assetOrCurrency, marketSize) => { };

    set assets(value) {
        this._assets = value;
        this._recalculateAssetOrCurrencyOptions();
        this._rerenderOptionsHTML();
    }

    set currencies(value) {
        this._currencies = value;
        this._recalculateAssetOrCurrencyOptions();
        this._rerenderOptionsHTML();
    }

    _assetOrCurrencyInput = undefined;

    connectedCallback() {
        console.log("Connected callback!");
        this._render();
        console.log("UL? ", this.querySelector("ul"));
    }

    _render() {
        this.innerHTML = `
        <drop-down-container>
            <div>
                <span data-drop-down-anchor>${this._assetOrCurrencyInput ?? 'Asset/Currency'}</span>
            </div>
            <ul class="border-1 rounded cursor-pointer bg-white max-h-[350px] overflow-y-auto overflow-x-none" data-drop-down-options>
                ${this._optionsHTML()}
            </ul>
        </drop-down-container>
        `;
    }

    _optionsHTML() {
        return this._assetOrCurrencyOptions.map(o =>
            `<li class="p-2 border-b-1 last:border-0" onclick="assetOrCurrencyClickHandler(o)">${o.name}</li>`)
            .join('\n');
    }

    _rerenderOptionsHTML() {
        const optionsContainer = this.querySelector("ul");
        if (optionsContainer) {
            optionsContainer.innerHTML = this._optionsHTML();
        }
    }

    _isAsset(assetOrCurrency) {
        return assetOrCurrency ? this._assets.find(a => a.name == assetOrCurrency) : false;
    }

    _isCurrency(assetOrCurrency) {
        return assetOrCurrency ? this._currencies.find(c => c.name == assetOrCurrency) : false;
    }

    _recalculateAssetOrCurrencyOptions() {
        const assetOrCurrencyOptions = [];
        this._assets.forEach(a => assetOrCurrencyOptions.push(a));
        this._currencies.forEach(c => assetOrCurrencyOptions.push(c));
        this._assetOrCurrencyOptions = assetOrCurrencyOptions;
    }
}

export function register() {
    customElements.define("markets-comparator-input-wc", MarketsComparatorInput);
}