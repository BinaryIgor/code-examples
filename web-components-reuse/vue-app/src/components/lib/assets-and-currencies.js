import { BaseHTMLElement } from "./base.js";

class AssetsAndCurrencies extends BaseHTMLElement {

    _assets = [];
    _assetsValueChangeReason = undefined;
    _currencies = [];
    _denomination = "USD";
    _assetsContainer = undefined;
    _currenciesContainer = undefined;

    set assets(value) {
        this._assets = value;
        this._renderAssets();
    }

    set assetsValueChangeReason(value) {
        this._assetsValueChangeReason = value;
        this._renderAssets();
    }

    set currencies(value) {
        this._currencies = value;
        this._renderCurrencies();
    }

    set denomination(value) {
        this._denomination = value;
        this._renderAssets();
        this._renderCurrencies();
    }

    connectedCallback() {
        this.innerHTML = `
        <div class="m-4">
            <tabs-container active-tab-class="underline">
                <div class="flex" data-tabs-header>
                    <tab-header>${this.translation('assets-header')}</tab-header>
                    <tab-header>${this.translation('currencies-header')}</tab-header>
                </div>
                <div data-tabs-body>
                    <div class="h-[40dvh] overflow-y-auto">
                    ${this._assetsHTML()}
                    </div>
                    <div class="h-[40dvh] overflow-y-auto">
                    ${this._currenciesHTML()}
                    </div>
                </div>
            </tabs-container>
        </div>`;

        const tabsBody = this.querySelector("[data-tabs-body]");
        this._assetsContainer = tabsBody.children[0];
        this._currenciesContainer = tabsBody.children[1];
    }

    _assetsHTML(previousAssetElements = []) {
        return this._assets.map(a => {
            const previousAsset = previousAssetElements.find(pa => pa.id == a.id);
            let previousMarketSize;
            if (!previousAsset) {
                previousMarketSize = a.marketSize;
            } else {
                const previousCurrencyMarketSize = previousAsset.getAttribute("market-size");
                if (previousCurrencyMarketSize != a.marketSize) {
                    previousMarketSize = previousCurrencyMarketSize;
                } else {
                    previousMarketSize = previousAsset.getAttribute("previous-market-size");
                }
            }

            return `<asset-element class="my-2" id="${a.id}" name="${a.name}"
                market-size="${a.marketSize}" previous-market-size="${previousMarketSize}"
                denomination="${a.denomination}"
                value-change-reason="${this._assetsValueChangeReason}"
                ${this.translationAttribute('market-size-label')}
                ${this.translationAttribute('previous-market-size-label')}
                ${this.translationAttribute('up-by-info')}
                ${this.translationAttribute('down-by-info')}>
            </asset-element>`;
        }).join("\n");
    }

    _currenciesHTML(previousCurrencyElements = []) {
        return this._currencies.map(c => {
            const previousCurrency = previousCurrencyElements.find(pc => pc.id == c.id);
            let previousMarketSize;
            if (!previousCurrency) {
                previousMarketSize = c.marketSize;
            } else {
                const previousCurrencyMarketSize = previousCurrency.getAttribute("market-size");
                if (previousCurrencyMarketSize != c.marketSize) {
                    previousMarketSize = previousCurrencyMarketSize;
                } else {
                    previousMarketSize = previousCurrency.getAttribute("previous-market-size");
                }
            }

            return `<currency-element class="my-2" id="${c.id}" name="${c.name}"
                market-size="${c.marketSize}" previous-market-size="${previousMarketSize}"
                denomination="${c.denomination}"
                ${this.translationAttribute('daily-turnover-label')}
                ${this.translationAttribute('yearly-turnover-label')}
                ${this.translationAttribute('up-by-info')}
                ${this.translationAttribute('down-by-info')}>
            </currency-element>`})
            .join("\n");
    }

    _renderAssets() {
        if (this._assetsContainer) {
            const currentAssetElements = [...this.querySelectorAll("asset-element")];
            this._assetsContainer.innerHTML = this._assetsHTML(currentAssetElements);
        }
    }

    _renderCurrencies() {
        if (this._currenciesContainer) {
            const currentCurrencyElements = [...this.querySelectorAll("currency-element")];
            this._currenciesContainer.innerHTML = this._currenciesHTML(currentCurrencyElements);
        }
    }
}

export function register() {
    customElements.define('assets-and-currencies', AssetsAndCurrencies);
}