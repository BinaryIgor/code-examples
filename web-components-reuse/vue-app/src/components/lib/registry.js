import * as AssetElement from './asset-element.js';
import * as CurrencyElement from './currency-element.js';
import * as TabsContainer from './tabs-container.js';
import * as DropDown from './drop-down.js';
import * as MarketsComparatorInput from './markets-comparator-input.js';
import * as Customizations from './customizations.js';


export function registerComponents() {
    AssetElement.register();
    CurrencyElement.register();
    TabsContainer.register();
    DropDown.register();
    MarketsComparatorInput.register();
    Customizations.register();
}