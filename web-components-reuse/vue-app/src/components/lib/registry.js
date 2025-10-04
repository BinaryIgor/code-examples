import * as AssetElement from './asset-element.js';
import * as CurrencyElement from './currency-element.js';
import * as TabsContainer from './tabs-container.js';
import * as DropDown from './drop-down.js';
import * as MarketsHeader from './markets-header.js';
import * as AssetsAndCurrencies from './assets-and-currencies.js';
import * as MarketsComparator from './markets-comparator.js';
import * as ProjectionsCalculator from './projections-calculator.js';
import * as MarketsProjections from './markets-projections.js';


export function registerComponents() {
    AssetElement.register();
    CurrencyElement.register();
    TabsContainer.register();
    DropDown.register();
    MarketsHeader.register();
    AssetsAndCurrencies.register();
    MarketsComparator.register();
    ProjectionsCalculator.register();
    MarketsProjections.register();
}