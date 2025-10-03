import { formatMoney, BaseHTMLElement } from './base.js';


class AssetElement extends BaseHTMLElement {

    /**
     * Supported attributes
     * {string} id: asset id
     * {string} name: asset name
     * {number} market-size
     * {number} previous-market-size
     * {string} value-change-reason: optional reason of the market size change
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
        const [id, name, marketSize, previousMarketSize, denomination, valueChangeReason] = [this.getAttribute("id"), this.getAttribute("name"),
        this.getAttribute("market-size"), this.getAttribute("previous-market-size"), this.getAttribute("denomination"), this.getAttribute("value-change-reason")
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
            <div>
                <span class="w-1/2 inline-block">${this.translation("previous-market-size-label")}:</span><span class="underline text-right w-1/2 inline-block">${formatMoney(this._previousMarketSize, denomination)}</span>
            <div>
            <p class="text-right"><span class="italic">${marketIsUp ? this.translation('market-up-by') : this.translation('market-down-by')} ${marketPercentageDiff}%</span>; ${valueChangeReason ?? 'UNKNOWN'}</p>`;
        } else {
            previousMarketSizeComponent = ``;
        }

        this.innerHTML = `
        <div data-id=${id} class="border-1 p-2 rounded-lg ${classesToAppend ? classesToAppend : ""}">
            <p class="font-bold">${name}</p>
            <div>
                <span class="w-1/2 inline-block">${this.translation('market-size-label')}:</span><span class="underline text-right w-1/2 inline-block">${formatMoney(marketSize, denomination)}</span>
            </div>
            ${previousMarketSizeComponent}
        </div>
        `;
    }
}

export function register() {
    customElements.define("asset-element", AssetElement);
}