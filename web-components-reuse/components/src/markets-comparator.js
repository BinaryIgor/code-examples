// TODO: document types!
class MarketsComparator extends HTMLElement {

    static observedAttributes = ["asset-items", "currency-items"];

    _fromInputId = crypto.randomUUID();
    _toInputId = crypto.randomUUID();

    _marketsComparatorInputFrom = null;
    _marketsComparatorInputTo = null;

    _fromMarketSize = null;
    _toMarketSize = null;

    set assets(value) {
        if (this._marketsComparatorInputFrom) {
            this._setValuesUsingAttributes(this._marketsComparatorInputFrom, value, "asset");
        }
        if (this._marketsComparatorInputTo) {
            this._setValuesUsingAttributes(this._marketsComparatorInputTo, value, "asset");
        }
    }

    set currencies(value) {
        if (this._marketsComparatorInputFrom) {
            this._setValuesUsingAttributes(this._marketsComparatorInputFrom, value, "currency");
        }
        if (this._marketsComparatorInputTo) {
            this._setValuesUsingAttributes(this._marketsComparatorInputTo, value, "currency");
        }
    }

    _setValuesUsingAttributes(element, values, prefix) {
        for (let i = 0; i < values.length; i++) {
            element.setAttribute(`${prefix}-${i}-name`, values[i].name);
            element.setAttribute(`${prefix}-${i}-market-size`, values[i].marketSize);
        }
        element.setAttribute(`${prefix}-items`, values.length);
    }

    connectedCallback() {
        this._render();

        this._chosenMarketSizeChangedEventHandler = e => {
            const { componentId, name, marketSize } = e.detail;
            if (componentId == this._fromInputId) {
                this._fromMarketSize = marketSize;
                this._renderComparisonElementHTML();
                document.dispatchEvent(new CustomEvent("mc:from-market-size-changed", {
                    detail: {
                        componentId: this.id,
                        name, marketSize
                    }
                }));
            } else if (componentId == this._toInputId) {
                this._toMarketSize = marketSize;
                this._renderComparisonElementHTML();
                document.dispatchEvent(new CustomEvent("mc:to-market-size-changed", {
                    detail: {
                        componentId: this.id,
                        name, marketSize
                    }
                }));
            }
        }
        document.addEventListener('mci:chosen-market-size-changed', this._chosenMarketSizeChangedEventHandler);
    }

    disconnectedCallback() {
        document.removeEventListener('mci:chosen-market-size-changed', this._chosenMarketSizeChangedEventHandler);
    }

    attributeChangedCallback(name, oldValue, newValue) {
        if (name == 'asset-items') {
            this.assets = assetsFromAttributes(this);
        } else if (name == 'currency-items') {
            this.currencies = currenciesFromAttributes(this);
        }
    }

    _render() {
        this._fromInputId = crypto.randomUUID();
        this._toInputId = crypto.randomUUID();
        this._comparisonElementId = crypto.randomUUID();

        this.innerHTML = `
        <div class="rounded border-1 p-2">
            <markets-comparator-input id=${this._fromInputId} options-z-index="101"></markets-comparator-input>
            <div class="py-4">to</div>
            <markets-comparator-input id=${this._toInputId} options-z-index="100"></markets-comparator-input>
            <div id=${this._comparisonElementId}>${this._comparisonElementHTML()}</div>
        </div>
        `;

        this._marketsComparatorInputFrom = document.getElementById(this._fromInputId);
        this._marketsComparatorInputTo = document.getElementById(this._toInputId);
    }

    _comparisonElementHTML() {
        if (!this._fromMarketSize || !this._toMarketSize) {
            return '<span class="text-xl my-4">-<span>';
        }
        return `<span class="text-xl my-4 underline">${this._fromMarketSize.toExponential(3)} / ${this._toMarketSize.toExponential(3)} = ${this._chosenMarketsComparedValue()}</span>`;
    }

    _chosenMarketsComparedValue() {
        if (!this._fromMarketSize || !this._toMarketSize) {
            return 0;
        }
        return Math.round(this._fromMarketSize * 1000 / this._toMarketSize) / 1000.0;
    }

    _renderComparisonElementHTML() {
        document.getElementById(this._comparisonElementId).innerHTML = this._comparisonElementHTML();
    }
}

function assetsFromAttributes(element) {
    const countAttribute = element.getAttribute("asset-items");
    if (!countAttribute) {
        return [];
    }
    const assets = [];
    for (let i = 0; i < parseInt(countAttribute); i++) {
        const name = element.getAttribute(`asset-${i}-name`);
        const marketSize = element.getAttribute(`asset-${i}-market-size`);
        if (name && marketSize) {
            assets.push({ name, marketSize: parseInt(marketSize) });
        }
    }
    return assets;
}

function currenciesFromAttributes(element) {
    const countAttribute = element.getAttribute("currency-items");
    if (!countAttribute) {
        return [];
    }
    const currencies = [];
    for (let i = 0; i < parseInt(countAttribute); i++) {
        const name = element.getAttribute(`currency-${i}-name`);
        const marketSize = element.getAttribute(`currency-${i}-market-size`);
        if (name && marketSize) {
            currencies.push({ name, marketSize: parseInt(marketSize) });
        }
    }
    return currencies;
}

class MarketsComparatorInput extends HTMLElement {

    static observedAttributes = ["asset-items", "currency-items"];

    _assets = [];
    _currencies = [];
    _assetOrCurrencyOptions = [];

    set assets(value) {
        this._assets = value;
        this._recalculateAssetOrCurrencyOptions();
    }

    set currencies(value) {
        this._currencies = value;
        this._recalculateAssetOrCurrencyOptions();
    }

    _assetOrCurrencyInput = null;
    _curencyTurnoverInputMultiplier = 1;

    connectedCallback() {
        this._render();
    }

    attributeChangedCallback(name, oldValue, newValue) {
        if (name == 'asset-items') {
            this.assets = assetsFromAttributes(this);
        } else if (name == 'currency-items') {
            this.currencies = currenciesFromAttributes(this);
        }
    }

    _render() {
        const optionsZIndex = this.getAttribute("options-z-index") ?? '99';
        this.innerHTML = `
        <drop-down-container options-z-index="${optionsZIndex}">
            <div data-drop-down-header>
                ${this._dropDownHeaderHTML()}
            </div>
            <ul class="border-1 rounded cursor-pointer bg-white max-h-[350px] overflow-y-auto overflow-x-none" data-drop-down-options>
                ${this._optionsHTML()}
            </ul>
        </drop-down-container>
        `;

        this._setOptionsClickHandlers();

        this.querySelector('[data-drop-down-header]')
            .querySelector("input")?.addEventListener("input", e => {
                this._curencyTurnoverInputMultiplier = e.target.value;
                this._calculateChosenMarketSizeChange();
            });
    }

    _optionsHTML() {
        return this._assetOrCurrencyOptions.map(o =>
            `<li class="p-2 border-b-1 last:border-0" data-option-id="${o.name}">${o.name}</li>`)
            .join('\n');
    }

    _dropDownHeaderHTML() {
        let marketSizeHTML;
        if (this._isAsset()) {
            marketSizeHTML = `<span>market size</span`;
        } else if (this._isCurrency()) {
            marketSizeHTML = `
            <span>
                <input type="number" class="mx-2 px-2 max-w-[75px]" value="${this._curencyTurnoverInputMultiplier}">
                <span>days turnover</span>
            </span>`;
        } else {
            marketSizeHTML = ``;
        }
        return `
        <span data-drop-down-anchor>${this._assetOrCurrencyInput ?? 'Asset/Currency'}</span>
        ${marketSizeHTML}
        `;
    }

    _setOptionsClickHandlers() {
        [...this.querySelectorAll("li")].forEach(o => {
            o.onclick = () => {
                this._assetOrCurrencyInput = o.getAttribute("data-option-id");
                this._calculateChosenMarketSizeChange();
                this._render();
            };
        });
    }

    _renderOptionsHTML() {
        const optionsContainer = this.querySelector("ul");
        if (optionsContainer) {
            optionsContainer.innerHTML = this._optionsHTML();
            this._setOptionsClickHandlers();
        }
    }

    _isAsset() {
        return this._assetOrCurrencyInput ? this._assets.find(a => a.name == this._assetOrCurrencyInput) : false;
    }

    _isCurrency() {
        return this._assetOrCurrencyInput ? this._currencies.find(c => c.name == this._assetOrCurrencyInput) : false;
    }

    _recalculateAssetOrCurrencyOptions() {
        const assetOrCurrencyOptions = [];
        this._assets.forEach(a => assetOrCurrencyOptions.push(a));
        this._currencies.forEach(c => assetOrCurrencyOptions.push(c));
        this._assetOrCurrencyOptions = assetOrCurrencyOptions;

        this._renderOptionsHTML();
        this._calculateChosenMarketSizeChange();
    }

    _calculateChosenMarketSizeChange() {
        if (!this._assetOrCurrencyInput) {
            return;
        }

        const assetInput = this._assets.find(a => a.name == this._assetOrCurrencyInput);
        const currencyInput = this._currencies.find(c => c.name == this._assetOrCurrencyInput);

        const inputMarketSize = assetInput ? assetInput.marketSize : currencyInput.marketSize * this._curencyTurnoverInputMultiplier;

        document.dispatchEvent(new CustomEvent("mci:chosen-market-size-changed", {
            detail: {
                componentId: this.id,
                name: this._assetOrCurrencyInput,
                marketSize: inputMarketSize
            }
        }));
    }
}

export function register() {
    customElements.define("markets-comparator-input", MarketsComparatorInput);
    customElements.define('markets-comparator', MarketsComparator);
}