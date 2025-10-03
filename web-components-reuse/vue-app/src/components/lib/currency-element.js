// TODO: better styling & translations
import * as Utils from './base.js';

class CurrencyElement extends HTMLElement {

    /**
     * Supported attributes
     * {string} id: currency id
     * {string} name: currency name
     * {number} market-size
     * {number} previous-market-size
     * {string} denomination
     * {string} class: additional class to append to the root div
     */
    connectedCallback() {
        this._render();
    }

    attributeChangedCallback(name, oldValue, newValue) {
        this._render();
    }

    _render() {
        const [id, name, marketSize, previousMarketSize, denomination] = [this.getAttribute("id"), this.getAttribute("name"),
        this.getAttribute("market-size"), this.getAttribute("previous-market-size"), this.getAttribute("denomination")
        ];
        if (id == undefined || name == undefined) {
            return;
        }
        const classesToAppend = this.getAttribute("class");

        if (previousMarketSize) {
            this._previousMarketSize = previousMarketSize;
        }

        let previousMarketSizeComponent;
        if (this._previousMarketSize && this._previousMarketSize != marketSize) {
            const previousMarketSizeInt = parseInt(this._previousMarketSize);
            const currentMarketSizeInt = parseInt(marketSize);
            const marketIsUp = currentMarketSizeInt > previousMarketSizeInt;
            let marketPercentageDiff;
            if (marketIsUp) {
                marketPercentageDiff = Math.round((currentMarketSizeInt - previousMarketSizeInt) * 100 * 100 / previousMarketSizeInt) / 100.0;
            } else {
                marketPercentageDiff = Math.round((previousMarketSizeInt - currentMarketSizeInt) * 100 * 100 / currentMarketSizeInt) / 100.0;
            }
            previousMarketSizeComponent = `
            <p class="text-right italic">${marketIsUp ? 'UP' : 'DOWN'} by ${marketPercentageDiff}%</p>`;
        } else {
            previousMarketSizeComponent = ``;
        }

        this.innerHTML = `
        <div data-id=${id} class="border-1 p-2 rounded-lg ${classesToAppend ? classesToAppend : ""}">
            <p class="font-bold">${name}</p>
            <div>
                <span class="w-1/2 inline-block">Daily turnover:</span><span class="underline text-right w-1/2 inline-block">${Utils.formatMoney(marketSize, denomination)}</span>
                </div>
            <div>
                <span class="w-1/2 inline-block">Yearly turnover:</span><span class="underline text-right w-1/2 inline-block">${Utils.formatMoney(`${365 * parseInt(marketSize)}`, denomination)}</span>
            </div>
            ${previousMarketSizeComponent}
        </div>
        `;
    }
}

export function register() {
    customElements.define("currency-element", CurrencyElement);
}