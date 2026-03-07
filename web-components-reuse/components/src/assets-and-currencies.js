import { BaseHTMLElement } from "./base.js";

/**
* @typedef {Object} AssetOrCurrencyElement
* @property {string} id
* @property {string} name
* @property {number} marketSize
* @property {string} denomination
*/

class AssetsAndCurrencies extends BaseHTMLElement {

  #assets = [];
  #assetsValueChangeReason = null;
  #currencies = [];
  #denomination = "USD";
  #assetsContainer = null;
  #currenciesContainer = null;

  /** @type {AssetOrCurrencyElement[]} */
  set assets(value) {
    this.#assets = value;
    this.#renderAssets();
  }

  set assetsValueChangeReason(value) {
    this.#assetsValueChangeReason = value;
    this.#renderAssets();
  }

  /** @type {AssetOrCurrencyElement[]} */
  set currencies(value) {
    this.#currencies = value;
    this.#renderCurrencies();
  }

  /** @type {string} */
  set denomination(value) {
    this.#denomination = value;
    this.#renderAssets();
    this.#renderCurrencies();
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
                    ${this.#assetsHTML()}
                    </div>
                    <div class="h-[40dvh] overflow-y-auto">
                    ${this.#currenciesHTML()}
                    </div>
                </div>
            </tabs-container>
        </div>`;

    const tabsBody = this.querySelector("[data-tabs-body]");
    this.#assetsContainer = tabsBody.children[0];
    this.#currenciesContainer = tabsBody.children[1];
  }

  #assetsHTML(previousAssetElements = []) {
    return this.#assets.map(a => {
      const previousAsset = previousAssetElements.find(pa => pa.id == a.id);
      let previousMarketSize;
      if (!previousAsset) {
        previousMarketSize = a.marketSize;
      } else {
        const previousAssetDenomination = previousAsset.getAttribute("denomination");
        const previousAssetMarketSize = previousAsset.getAttribute("market-size");
        // if denomination has changed, comparing current market size with the previous is meaningless
        if (previousAssetDenomination != a.denomination) {
          previousMarketSize = a.marketSize;
        } else if (previousAssetMarketSize != a.marketSize) {
          previousMarketSize = previousAssetMarketSize;
        } else {
          previousMarketSize = previousAsset.getAttribute("previous-market-size");
        }
      }

      return `<asset-element class="my-2" id="${a.id}" name="${a.name}"
                market-size="${a.marketSize}" previous-market-size="${previousMarketSize}"
                denomination="${a.denomination}"
                value-change-reason="${this.#assetsValueChangeReason}"
                ${this.translationAttribute('market-size-label')}
                ${this.translationAttribute('previous-market-size-label')}
                ${this.translationAttribute('up-by-info')}
                ${this.translationAttribute('down-by-info')}>
            </asset-element>`;
    }).join("\n");
  }

  #currenciesHTML(previousCurrencyElements = []) {
    return this.#currencies.map(c => {
      const previousCurrency = previousCurrencyElements.find(pc => pc.id == c.id);
      let previousMarketSize;
      if (!previousCurrency) {
        previousMarketSize = c.marketSize;
      } else {
        const previousCurrencyDenomination = previousCurrency.getAttribute("denomination");
        const previousCurrencyMarketSize = previousCurrency.getAttribute("market-size");
        // if denomination has changed, comparing current market size with the previous is meaningless
        if (previousCurrencyDenomination != c.denomination) {
          previousMarketSize = c.marketSize;
        } else if (previousCurrencyMarketSize != c.marketSize) {
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

  #renderAssets() {
    if (this.#assetsContainer) {
      const currentAssetElements = [...this.querySelectorAll("asset-element")];
      this.#assetsContainer.innerHTML = this.#assetsHTML(currentAssetElements);
    }
  }

  #renderCurrencies() {
    if (this.#currenciesContainer) {
      const currentCurrencyElements = [...this.querySelectorAll("currency-element")];
      this.#currenciesContainer.innerHTML = this.#currenciesHTML(currentCurrencyElements);
    }
  }
}

export function register() {
  customElements.define('assets-and-currencies', AssetsAndCurrencies);
}