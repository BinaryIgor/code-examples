import { formatMoney, BaseHTMLElement } from './base.js';

class AssetElement extends BaseHTMLElement {

  /**
   * Supported attributes
   * {string} id: asset id
   * {string} name: asset name
   * {number} market-size
   * {number} previous-market-size
   * {string} denomination
   * {string} class: additional class to append to the root div
   */
  connectedCallback() {
    this.#render();
  }

  attributeChangedCallback(name, oldValue, newValue) {
    this.#render();
  }

  #render() {
    const [id, name, marketSize, previousMarketSize, denomination] = [this.getAttribute("id"), this.getAttribute("name"),
    this.getAttribute("market-size"), this.getAttribute("previous-market-size"), this.getAttribute("denomination")];
    if (!id || !name) {
      return;
    }
    const classesToAppend = this.getAttribute("class");

    let previousMarketSizeComponent;
    if (previousMarketSize && previousMarketSize != marketSize) {
      const previousMarketSizeInt = parseInt(previousMarketSize);
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
                <span class="w-1/2 inline-block">${this.translation("previous-market-size-label")}:</span><span class="underline text-right w-1/2 inline-block">${formatMoney(previousMarketSize, denomination)}</span>
            <div>
            <p class="text-right italic">${marketIsUp ? this.translation('up-by-info') : this.translation('down-by-info')} ${marketPercentageDiff}%</p>`;
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