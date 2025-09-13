class MarketsComparatorInput extends HTMLElement {

    _assets = [];
    _currencies = [];
    _onChosenMarketSizeChange = (assetOrCurrency, marketSize) => { };

    _assetOrCurrencyInput = undefined;

    connectedCallback() {
        console.log("Markets comparator input!", this._assets, this._currencies);
        this.innerHTML = `
        <drop-down-container>
            <div>
                <span data-drop-down-anchor>${ this._assetOrCurrencyInput ?? 'Asset/Currency' }</span>
            </div>
            <ul class="border-1 rounded cursor-pointer bg-white max-h-[350px] overflow-y-auto overflow-x-none" data-drop-down-options>
            </ul>
        </drop-down-container>
        `;
    }

    _isAsset(assetOrCurrency) {
        return assetOrCurrency ? this._assets.find(a => a.name == assetOrCurrency) : false;
    }

    _isCurrency(assetOrCurrency) {
        return assetOrCurrency ? this._currencies.find(c => c.name == assetOrCurrency) : false;
    }
}

export function register() {
    customElements.define("markets-comparator-input-wc", MarketsComparatorInput);
}