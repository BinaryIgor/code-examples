import { BaseHTMLElement } from "./base";

/**
* @typedef {Object} AssetOrCurrency
* @property {string} name
* @property {number} marketSize
*/

class MarketsProjections extends BaseHTMLElement {

    _marketsComparatorComponent = null;
    _projectionsCalculatorComponent = null;

    /** @type {?AssetOrCurrency} */
    _fromAssetOrCurrency = null;
    /** @type {?AssetOrCurrency} */
    _toAssetOrCurrency = null;

    /** @type {AssetOrCurrency[]} */
    set assets(value) {
        if (this._marketsComparatorComponent) {
            this._marketsComparatorComponent.assets = value;
        }
    }

    /** @type {AssetOrCurrency[]} */
    set currencies(value) {
        if (this._marketsComparatorComponent) {
            this._marketsComparatorComponent.currencies = value;
        }
    }

    connectedCallback() {
        this.innerHTML = `
        <div class="rounded border-1 p-2 mt-16 mx-4 mb-8">
            <h2 class="text-2xl mb-8">${this.translation('projections-header')}</h2>
            <markets-comparator
                ${this.translationAttributeRemovingNamespace('asset-or-currency-input-placeholder', 'markets-comparator.')}
                ${this.translationAttributeRemovingNamespace('market-size-input-label', 'markets-comparator.')}
                ${this.translationAttributeRemovingNamespace('days-turnover-input-label', 'markets-comparator.')}
                ${this.translationAttributeRemovingNamespace('markets-to', 'markets-comparator.')}>
            </markets-comparator>
            <projections-calculator 
                ${this.translationAttributeRemovingNamespace('asset-or-currency-placeholder', 'projections-calculator.')}
                ${this.translationAttributeRemovingNamespace('asset-or-currency-expected-annual-growth-rate', 'projections-calculator.')}
                ${this.translationAttributeRemovingNamespace('results-in-header', 'projections-calculator.')}
                ${this.translationAttributeRemovingNamespace('year', 'projections-calculator.')}
                ${this.translationAttributeRemovingNamespace('years', 'projections-calculator.')}>
            </projections-calculator>
        </div>
        `;

        this._marketsComparatorComponent = this.querySelector("markets-comparator");
        this._projectionsCalculatorComponent = this.querySelector("projections-calculator");

        const fromMarketSizeChangedEventHandler = e => {
            this._fromAssetOrCurrency = e.detail;
            this._projectionsCalculatorComponent.assetOrCurrency1 = this._fromAssetOrCurrency;
        };
        const toMarketSizeChangedEventHandler = e => {
            this._toAssetOrCurrency = e.detail;
            this._projectionsCalculatorComponent.assetOrCurrency2 = this._toAssetOrCurrency;
        };

        this.addEventListener('mc.from-market-size-changed', fromMarketSizeChangedEventHandler);
        this.addEventListener('mc.to-market-size-changed', toMarketSizeChangedEventHandler);
    }
}

export function register() {
    customElements.define('markets-projections', MarketsProjections);
}