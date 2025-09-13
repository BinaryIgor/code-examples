// TODO: better styling & translations
import * as Utils from './utils.js';

class AssetElement extends HTMLElement {

    static observedAttributes = ["market-size", "denomination", "value-change-reasons"];
    _previousMarketSize = null;

    connectedCallback() {
        this._render();
    }

    attributeChangedCallback(name, oldValue, newValue) {
        this._render();
        if (name == 'market-size') {
            this._previousMarketSize = newValue;
        }
    }

    _render() {
        const [id, name, marketSize, denomination, valueChangeReasons] = [this.getAttribute("id"), this.getAttribute("name"),
        this.getAttribute("market-size"), this.getAttribute("denomination"), this.getAttribute("value-change-reasons")
        ];
        if (id == undefined || name == undefined) {
            return;
        }
        const classesToAppend = this.getAttribute("class");

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
            <div>
                <span class="w-1/2 inline-block">Previous market size:</span><span class="underline text-right w-1/2 inline-block">${Utils.formatMoney(this._previousMarketSize, denomination)}</span>
            <div>
            <p class="text-right"><span class="italic">${marketIsUp ? 'UP' : 'DOWN'} by ${marketPercentageDiff}%</span>; ${valueChangeReasons ?? 'UNKNOWN'}</p>`;
        } else {
            previousMarketSizeComponent = ``;
        }

        this.innerHTML = `
        <div data-id=${id} class="border-1 p-2 rounded-lg ${classesToAppend ? classesToAppend : ""}">
            <p class="font-bold">${name}</p>
            <div>
                <span class="w-1/2 inline-block">Market size:</span><span class="underline text-right w-1/2 inline-block">${Utils.formatMoney(marketSize, denomination)}</span>
            </div>
            ${previousMarketSizeComponent}
        </div>
        `;
    }
}

export function register() {
    customElements.define("asset-element", AssetElement);
}