import { BaseHTMLElement } from "./base.js";

/**
* @typedef {Object} AssetOrCurrency
* @property {string} name
* @property {number} marketSize
*
* @typedef {Object} AssetOrCurrencyProjection
* @property {number} marketSize
* @property {number} growthRate
*/

class ProjectionsCalculator extends BaseHTMLElement {

    /** @type {?AssetOrCurrency} */
    _assetOrCurrency1 = null;
    /** @type {?AssetOrCurrency} */
    _assetOrCurrency2 = null;
    /** @type {?number} */
    _assetOrCurrency1ExpectedGrowthRate = null;
    /** @type {?number} */
    _assetOrCurrency2ExpectedGrowthRate = null;
    /** @type {?number} */
    _customProjectionYears = null;

    set assetOrCurrency1(value) {
        this._assetOrCurrency1 = value;
        this._renderProjectionsResultsHTML();
    }

    set assetOrCurrency2(value) {
        this._assetOrCurrency2 = value;
        this._renderProjectionsResultsHTML();
    }

    _assetOrCurrency1Header = null;
    _assetOrCurrency1Input = null;
    _assetOrCurrency2Header = null;
    _assetOrCurrency2Input = null;
    _customProjectionInput = null;
    _projectionsResultsContainer = null;
    _customProjectionContainer = null;
    _customProjectionTextElement = null;
    _customProjectionResultContainer = null;

    connectedCallback() {
        this.innerHTML = `
        <div>
            <div>${this._assetOrCurrencyHeaderText(this._assetOrCurrency1)}</div>
            <input type="number" class="px-2 cursor-pointer" placeholder="%" value=${this._assetOrCurrency1ExpectedGrowthRate ?? ''}>
            <div>${this._assetOrCurrencyHeaderText(this._assetOrCurrency2)}</div>
            <input type="number" class="px-2 cursor-pointer" placeholder="%" value=${this._assetOrCurrency2ExpectedGrowthRate ?? ''}>
            <div>
                ${this._projectionsResultsHTML()}
            </div>
            <div>
                ${this._customProjectionHTML()}
            </div>
        </div>
        `;

        const container = this.querySelector("div");

        const divs = container.querySelectorAll("div");
        [this._assetOrCurrency1Header, this._assetOrCurrency2Header, this._projectionsResultsContainer] = divs;
        this._customProjectionContainer = divs[divs.length - 1];

        [this._assetOrCurrency1Input, this._assetOrCurrency2Input,
        this._customProjectionInput] = container.querySelectorAll("input");

        this._assetOrCurrency1Input.addEventListener("input", e => {
            this._assetOrCurrency1ExpectedGrowthRate = parseInt(e.target.value);
            this._renderProjectionsResultsHTML();
        });
        this._assetOrCurrency2Input.addEventListener("input", e => {
            this._assetOrCurrency2ExpectedGrowthRate = parseInt(e.target.value);
            this._renderProjectionsResultsHTML();
        });
        this._customProjectionInput.addEventListener("input", e => {
            this._customProjectionYears = parseInt(e.target.value);
            this._updateCustomProjectionText();
            this._updateCustomProjectionResult();
        });

        this._customProjectionTextElement = this.querySelector('[data-custom-projection-text-element]');
        this._customProjectionResultContainer = this.querySelector('[data-custom-projection-result-container]');
    }

    _assetOrCurrencyHeaderText(assetOrCurrency) {
        return `${assetOrCurrency ? assetOrCurrency.name : this.translation('asset-or-currency-placeholder')} ${this.translation('asset-or-currency-expected-annual-growth-rate')}:`;
    }

    _renderProjectionsResultsHTML() {
        if (this._projectionsResultsContainer && this._customProjectionContainer) {
            this._projectionsResultsContainer.innerHTML = this._projectionsResultsHTML();
            this._updateCustomProjectionResult();
        }
    }

    _projectionsResultsHTML() {
        const inYearsText = (years) => `${this.translation('results-in-header')} ${years} ${this.translation(years == 1 ? 'year' : 'years')}`;
        const currentYear = new Date().getFullYear();
        return [1, 5, 10].map(y => `
            <div class="mt-t">${inYearsText(y)} (${currentYear + y}):</div>
            ${this._projectionsResultHTML(y)}`)
            .join('\n');
    }

    _projectionsResultHTML(years) {
        const ac1 = this._assetOrCurrency1WithExpectedGrowthRate();
        const ac2 = this._assetOrCurrency2WithExpectedGrowthRate();
        if (years != null && ac1 != null && ac2 != null) {
            return `
            <projections-result years=${years}
                asset-or-currency-1-market-size="${ac1.marketSize}"
                asset-or-currency-1-growth-rate="${ac1.growthRate}"
                asset-or-currency-2-market-size="${ac2.marketSize}"
                asset-or-currency-2-growth-rate="${ac2.growthRate}">
            </projections-result>`;
        }
        return '<div>-</div>';
    }

    _customProjectionHTML() {
        return `
        ${this.translation('results-in-header')} <input class="max-w-[60px] px-2" type="number">
        <span data-custom-projection-text-element>${this._customProjectionYearText()}</span>:
        <div data-custom-projection-result-container>${this._projectionsResultHTML(this._customProjectionYears)}</div>
        `;
    }

    _customProjectionYearText() {
        const currentYear = new Date().getFullYear();
        return '(' + (this._customProjectionYears == null || Number.isNaN(this._customProjectionYears) ?
            `${currentYear} + N` : (currentYear + this._customProjectionYears)) + ')';
    }

    _updateCustomProjectionText() {
        this._customProjectionTextElement.textContent = this._customProjectionYearText();
    }

    _updateCustomProjectionResult() {
        this._customProjectionContainer.innerHTML = this._projectionsResultHTML(this._customProjectionYears);
    }

    _assetOrCurrency1WithExpectedGrowthRate() {
        if (this._assetOrCurrency1 && this._assetOrCurrency1ExpectedGrowthRate != null) {
            return { marketSize: this._assetOrCurrency1.marketSize, growthRate: this._assetOrCurrency1ExpectedGrowthRate };
        }
        return null;
    }

    _assetOrCurrency2WithExpectedGrowthRate() {
        if (this._assetOrCurrency2 && this._assetOrCurrency2ExpectedGrowthRate != null) {
            return { marketSize: this._assetOrCurrency2.marketSize, growthRate: this._assetOrCurrency2ExpectedGrowthRate };
        }
        return null;
    }
}

class ProjectionsResult extends HTMLElement {

    static observedAttributes = [
        "years",
        "asset-or-currency-1-market-size", "asset-or-currency-1-growth-rate",
        "asset-or-currency-2-market-size", "asset-or-currency-2-growth-rate"
    ];

    /** @type {number} */
    _years = 1;
    /** @type {?AssetOrCurrencyProjection} */
    _assetOrCurrency1 = null;
    /** @type {?AssetOrCurrencyProjection} */
    _assetOrCurrency2 = null;

    set years(value) {
        this._years = value;
        this._render();
    }

    set assetOrCurrency1(value) {
        this._assetOrCurrency1 = value;
        this._render();
    }

    set assetOrCurrency2(value) {
        this._assetOrCurrency2 = value;
        this._render();
    }

    connectedCallback() {
        this._render();
    }

    attributeChangedCallback(name, oldValue, newValue) {
        if (name.includes("asset-or-currency-1")) {
            const assetOrCurrency = this._assetOrCurrencyFromAttributes("asset-or-currency-1");
            if (assetOrCurrency) {
                this.assetOrCurrency1 = assetOrCurrency;
            }
        } else if (name.includes("asset-or-currency-2")) {
            const assetOrCurrency = this._assetOrCurrencyFromAttributes("asset-or-currency-2");
            if (assetOrCurrency) {
                this.assetOrCurrency2 = assetOrCurrency;
            }
        } else if (name == 'years') {
            this.years = parseInt(newValue);
        }
    }

    _assetOrCurrencyFromAttributes(prefix) {
        const marketSize = this.getAttribute(`${prefix}-market-size`);
        const growthRate = this.getAttribute(`${prefix}-growth-rate`);
        if (marketSize && growthRate) {
            return { marketSize: parseInt(marketSize), growthRate: parseInt(growthRate) };
        }
        return null;
    }

    _render() {
        const nominator = this._exponentialNumberString(this._projectionNumerator());
        const denominator = this._exponentialNumberString(this._projectionDenominator());
        this.innerHTML = `
        <div class="underline">${nominator} / ${denominator} = ${this._projection()} 
        </div>
        `;
    }

    _exponentialNumberString(n) {
        return n?.toExponential(3) ?? '';
    }

    _marketSizeChangedByRate(marketSize, growthRate, decrease = false) {
        let changedMarketSize;
        if (decrease) {
            changedMarketSize = marketSize - (marketSize * growthRate / 100.0);
        } else {
            changedMarketSize = marketSize + (marketSize * growthRate / 100.0);
        }
        if (changedMarketSize <= 0 || Number.isNaN(changedMarketSize)) {
            return null;
        }
        return changedMarketSize;
    }

    _marketSizeChangedByRateInGivenYears(marketSize, growthRate, years) {
        const negativeYears = years < 0;
        let increasedMarketSize = marketSize;
        for (let i = 0; i < Math.abs(years); i++) {
            increasedMarketSize = this._marketSizeChangedByRate(increasedMarketSize, growthRate, negativeYears);
            if (!increasedMarketSize) {
                return null;
            }
        }
        return increasedMarketSize;
    }

    _projectionNumerator() {
        if (this._assetOrCurrency1) {
            return this._marketSizeChangedByRateInGivenYears(
                this._assetOrCurrency1.marketSize,
                this._assetOrCurrency1.growthRate,
                this._years);
        }
        return null;
    }

    _projectionDenominator() {
        if (this._assetOrCurrency2) {
            return this._marketSizeChangedByRateInGivenYears(
                this._assetOrCurrency2.marketSize,
                this._assetOrCurrency2.growthRate,
                this._years);
        }
        return null;
    }

    _projection() {
        const numerator = this._projectionNumerator();
        const denominator = this._projectionDenominator();
        if (numerator && denominator) {
            return Math.round(numerator * 1000 / denominator) / 1000.0;
        }
        return '';
    }
}

export function register() {
    customElements.define("projections-result", ProjectionsResult);
    customElements.define("projections-calculator", ProjectionsCalculator);
}